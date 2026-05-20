package com.lab03g10.smartpantry.data.repository

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.lab03g10.smartpantry.data.remote.dto.FoodItemDto
import com.lab03g10.smartpantry.data.remote.dto.SensorConfigDto
import com.lab03g10.smartpantry.data.remote.dto.ShoppingItemDto
import com.lab03g10.smartpantry.data.remote.dto.WasteLogDto
import com.lab03g10.smartpantry.data.remote.dto.toDto
import com.lab03g10.smartpantry.data.remote.dto.toEntity
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import com.lab03g10.smartpantry.domain.repo.ShoppingRepository
import com.lab03g10.smartpantry.domain.repo.WasteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-way Firestore backup (push on sign-out, pull on sign-in) per
 * Phase 16 of gap-closure-checklist.md.
 *
 * Scope is deliberately narrow:
 *   ✓ food_items, waste_log, shopping, sensors
 *   ✗ sensor_readings (volume blows the Spark plan quota)
 *   ✗ notifications (UNREAD state is device-scoped, shouldn't propagate)
 *
 * Both push and pull are best-effort: failures are logged and swallowed
 * so a flaky network never blocks sign-out or strands the user with a
 * crash on sign-in.
 */
@Singleton
class BackupRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val foodRepo: FoodRepository,
    private val wasteRepo: WasteRepository,
    private val shoppingRepo: ShoppingRepository,
    private val sensorRepo: SensorRepository,
) {

    /**
     * Push the entire local Room snapshot to /users/{uid}/<collection>/.
     * Batched per collection so partial failures only lose one batch.
     */
    suspend fun pushSnapshot(uid: String) = withContext(Dispatchers.IO) {
        runCatching {
            val userRef = firestore.collection("users").document(uid)

            pushFoods(userRef.collection(COLL_FOOD), foodRepo.observeAll().first())
            pushWaste(userRef.collection(COLL_WASTE), wasteRepo.observeAll().first())
            pushShopping(userRef.collection(COLL_SHOPPING), shoppingRepo.observeAll().first())
            pushSensors(userRef.collection(COLL_SENSORS), sensorRepo.observeConfigs().first())
        }.onFailure {
            Log.w(TAG, "pushSnapshot failed (best-effort): ${it.message}")
        }
    }

    /**
     * Pull the cloud snapshot and merge into Room with last-write-wins.
     * Per-collection merge semantics:
     *   - food_items: LWW by `updatedAt`
     *   - waste_log : append-only (id-dedupe; never overwrite)
     *   - shopping  : LWW by `addedAt` (no updatedAt; documented limitation)
     *   - sensors   : cloud-wins on id match (no mutation timestamp)
     *
     * Local rows absent from the cloud are always kept — the cloud
     * does not enforce deletion; the next pushSnapshot will re-upload.
     */
    suspend fun pullSnapshot(uid: String) = withContext(Dispatchers.IO) {
        runCatching {
            val userRef = firestore.collection("users").document(uid)

            mergeFoods(userRef.collection(COLL_FOOD))
            mergeWaste(userRef.collection(COLL_WASTE))
            mergeShopping(userRef.collection(COLL_SHOPPING))
            mergeSensors(userRef.collection(COLL_SENSORS))
        }.onFailure {
            Log.w(TAG, "pullSnapshot failed (best-effort): ${it.message}")
        }
    }

    // ---- push helpers (one per collection — keeps types concrete) ----------

    private suspend fun pushFoods(coll: CollectionReference, rows: List<com.lab03g10.smartpantry.data.local.entity.FoodItemEntity>) {
        if (rows.isEmpty()) return
        val batch = firestore.batch()
        rows.forEach { batch.set(coll.document(it.id), it.toDto()) }
        batch.commit().await()
    }

    private suspend fun pushWaste(coll: CollectionReference, rows: List<com.lab03g10.smartpantry.data.local.entity.WasteLogEntity>) {
        if (rows.isEmpty()) return
        val batch = firestore.batch()
        rows.forEach { batch.set(coll.document(it.id), it.toDto()) }
        batch.commit().await()
    }

    private suspend fun pushShopping(coll: CollectionReference, rows: List<com.lab03g10.smartpantry.data.local.entity.ShoppingItemEntity>) {
        if (rows.isEmpty()) return
        val batch = firestore.batch()
        rows.forEach { batch.set(coll.document(it.id), it.toDto()) }
        batch.commit().await()
    }

    private suspend fun pushSensors(coll: CollectionReference, rows: List<com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity>) {
        if (rows.isEmpty()) return
        val batch = firestore.batch()
        rows.forEach { batch.set(coll.document(it.id), it.toDto()) }
        batch.commit().await()
    }

    // ---- merge helpers -----------------------------------------------------

    private suspend fun mergeFoods(coll: CollectionReference) {
        val cloud = coll.get().await().toObjects(FoodItemDto::class.java)
        val localById = foodRepo.observeAll().first().associateBy { it.id }
        cloud.forEach { dto ->
            val local = localById[dto.id]
            if (local == null || dto.updatedAt > local.updatedAt) {
                foodRepo.upsert(dto.toEntity())
            }
        }
    }

    private suspend fun mergeWaste(coll: CollectionReference) {
        val cloud = coll.get().await().toObjects(WasteLogDto::class.java)
        val localIds = wasteRepo.observeAll().first().map { it.id }.toHashSet()
        cloud.forEach { dto ->
            // Append-only: existing rows are immutable so we never overwrite.
            if (dto.id !in localIds) {
                wasteRepo.log(dto.toEntity())
            }
        }
    }

    private suspend fun mergeShopping(coll: CollectionReference) {
        val cloud = coll.get().await().toObjects(ShoppingItemDto::class.java)
        val localById = shoppingRepo.observeAll().first().associateBy { it.id }
        cloud.forEach { dto ->
            val local = localById[dto.id]
            if (local == null || dto.addedAt > local.addedAt) {
                shoppingRepo.upsert(dto.toEntity())
            }
        }
    }

    private suspend fun mergeSensors(coll: CollectionReference) {
        val cloud = coll.get().await().toObjects(SensorConfigDto::class.java)
        val localById = sensorRepo.observeConfigs().first().associateBy { it.id }
        cloud.forEach { dto ->
            val local = localById[dto.id]
            // Cloud-wins: SensorConfigEntity carries no mutation timestamp,
            // so the user-visible contract is "your last-edited thresholds
            // follow you across devices". A short-circuit avoids needless
            // writes (and Flow re-emissions) when nothing actually changed.
            val sameAsLocal = local != null &&
                local.displayName == dto.displayName &&
                local.deviceId == dto.deviceId &&
                local.environmentType == dto.environmentType &&
                local.thresholdC == dto.thresholdC.toFloat() &&
                local.humidityThreshold == dto.humidityThreshold.toFloat() &&
                local.maxLux == dto.maxLux.toFloat() &&
                local.csvAssetName == dto.csvAssetName &&
                local.pairedAt == dto.pairedAt &&
                local.active == dto.active
            if (!sameAsLocal) {
                sensorRepo.upsertConfig(dto.toEntity())
            }
        }
    }

    companion object {
        private const val TAG = "BackupRepo"
        private const val COLL_FOOD = "food_items"
        private const val COLL_WASTE = "waste_log"
        private const val COLL_SHOPPING = "shopping"
        private const val COLL_SENSORS = "sensors"
    }
}
