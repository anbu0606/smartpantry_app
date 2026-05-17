package com.lab03g10.smartpantry.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import com.lab03g10.smartpantry.domain.repo.SensorRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * @(#) AddEditFoodViewModelSaveTest.kt 1.0 2026-05-17
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Covers the three-case fix from gap-closure-checklist §6.3 for
 * AddEditFoodViewModel.save(). The bug (introduced in a00483a)
 * unconditionally wrote `current.nominalExpiryDate` into
 * `effectiveExpiryDate`, clobbering any value ShelfLifeRecalcWorker
 * had already computed whenever the user opened an existing item in
 * edit mode and pressed Save without changing the expiry date.
 *
 * Tests target three paths:
 *   A — Add mode: effective = nominal
 *   B — Edit + user changed expiry: effective = new nominal (worker re-adjusts later)
 *   C — Edit + expiry untouched: effective = existing.effectiveExpiryDate (preserved)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddEditFoodViewModelSaveTest {

    // viewModelScope dispatches on Dispatchers.Main; UnconfinedTestDispatcher runs
    // continuations eagerly so init { } and save()'s launch complete before the
    // test thread moves on — no advanceUntilIdle() bookkeeping required.
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- date fixtures -----------------------------------------------------
    // Dates are computed relative to "now" rather than hard-coded so the
    // VM's expiry-in-the-past validator (AddEditFoodUiState.expiryError) never
    // flips the form to invalid as the calendar moves forward.
    private val day = 24L * 60 * 60 * 1000
    private val todayUtc = run {
        val now = System.currentTimeMillis()
        now - (now % day)   // UTC midnight, matching AddEditFoodViewModel.todayStartMillis()
    }
    private val nominalFuture       = todayUtc + 14 * day  // existing.nominalExpiryDate
    private val workerShiftedExpiry = todayUtc + 12 * day  // existing.effectiveExpiryDate (2 d earlier)
    private val userChangedExpiry   = todayUtc + 20 * day  // case B — user picks a new date
    private val addModeExpiry       = todayUtc + 10 * day  // case A — fresh add

    // ---- fakes -------------------------------------------------------------
    private val foodRepo = FakeFoodRepository()
    private val sensorRepo = FakeSensorRepository()

    // ---- Case C — the regression itself ------------------------------------
    @Test
    fun `editing existing food without changing expiry preserves worker-adjusted effective`() {
        foodRepo.setExisting(
            milk(
                id = "milk-1",
                nominalExpiryDate = nominalFuture,
                effectiveExpiryDate = workerShiftedExpiry,
            ),
        )

        val vm = AddEditFoodViewModel(
            savedStateHandle = SavedStateHandle(mapOf("foodId" to "milk-1")),
            foodRepository = foodRepo,
            sensorRepository = sensorRepo,
        )
        // init { } loads existing into the form synchronously under UnconfinedTestDispatcher

        vm.onNameChange("Almond Milk")  // change ONLY name — expiry stays loaded value
        vm.save()

        val saved = foodRepo.lastUpserted
            ?: error("save() did not upsert — form was not valid (check fixture dates)")
        assertEquals(
            "effectiveExpiryDate must preserve worker-adjusted value on no-op edit",
            workerShiftedExpiry,
            saved.effectiveExpiryDate,
        )
        // Sanity: nominal is unchanged too
        assertEquals(nominalFuture, saved.nominalExpiryDate)
    }

    // ---- Case B — explicit expiry edit overrides worker output -------------
    @Test
    fun `editing existing food and changing expiry resets effective to new nominal`() {
        foodRepo.setExisting(
            milk(
                id = "milk-1",
                nominalExpiryDate = nominalFuture,
                effectiveExpiryDate = workerShiftedExpiry,
            ),
        )

        val vm = AddEditFoodViewModel(
            savedStateHandle = SavedStateHandle(mapOf("foodId" to "milk-1")),
            foodRepository = foodRepo,
            sensorRepository = sensorRepo,
        )
        vm.onExpiryDateChange(userChangedExpiry)   // user actively shifts expiry
        vm.save()

        val saved = foodRepo.lastUpserted
            ?: error("save() did not upsert — form was not valid (check fixture dates)")
        assertEquals(
            "effectiveExpiryDate must follow the user's new nominal",
            userChangedExpiry,
            saved.effectiveExpiryDate,
        )
        // Worker will re-adjust on the next tick — that path is covered by ShelfLifeRecalcWorker tests.
    }

    // ---- Case A — add mode --------------------------------------------------
    @Test
    fun `add-mode food gets effectiveExpiry equal to nominal`() {
        val vm = AddEditFoodViewModel(
            savedStateHandle = SavedStateHandle(emptyMap()),
            foodRepository = foodRepo,
            sensorRepository = sensorRepo,
        )

        vm.onNameChange("Spinach")
        vm.onQuantityChange("200")
        vm.onExpiryDateChange(addModeExpiry)
        vm.save()

        val saved = foodRepo.lastUpserted
            ?: error("save() did not upsert — form was not valid (check fixture dates)")
        assertEquals(addModeExpiry, saved.nominalExpiryDate)
        assertEquals(addModeExpiry, saved.effectiveExpiryDate)
    }

    // ---- helpers -----------------------------------------------------------

    private fun milk(
        id: String,
        nominalExpiryDate: Long,
        effectiveExpiryDate: Long,
    ): FoodItemEntity = FoodItemEntity(
        id = id,
        name = "Whole Milk",
        category = "Dairy",
        quantity = "1",
        unit = "L",
        barcode = null,
        imageUrl = null,
        purchaseDate = todayUtc - 2 * day,
        nominalExpiryDate = nominalExpiryDate,
        effectiveExpiryDate = effectiveExpiryDate,
        storageLocationId = "sensor-fridge",
        notes = null,
        createdAt = todayUtc - 2 * day,
        updatedAt = todayUtc - 2 * day,
    )
}

// Minimal fakes — only the four methods AddEditFoodViewModel actually touches
// are implemented; anything else fails loudly so a future regression in the
// VM's repo usage doesn't silently slip through these tests.
private class FakeFoodRepository : FoodRepository {
    private val itemFlow = MutableStateFlow<FoodItemEntity?>(null)

    /** Last entity passed to [upsert] — null if save() short-circuited on isValid. */
    var lastUpserted: FoodItemEntity? = null
        private set

    fun setExisting(item: FoodItemEntity) {
        itemFlow.value = item
    }

    override fun observeById(id: String): Flow<FoodItemEntity?> = itemFlow

    override suspend fun upsert(item: FoodItemEntity) {
        lastUpserted = item
        itemFlow.value = item    // keep observeById consistent for any follow-up reads
    }

    override fun observeAll(): Flow<List<FoodItemEntity>> = error("not used by AddEditFoodViewModel")
    override fun observeUseToday(today: Long): Flow<List<FoodItemEntity>> = error("not used")
    override fun observeByCategory(category: String): Flow<List<FoodItemEntity>> = error("not used")
    override suspend fun findByBarcode(barcode: String): FoodItemEntity? = error("not used")
    override suspend fun delete(id: String) = error("not used")
    override suspend fun markConsumed(id: String) = error("not used")
    override suspend fun markWasted(id: String) = error("not used")
    override suspend fun findActiveByStorageLocation(sensorId: String): List<FoodItemEntity> = error("not used")
    override suspend fun updateEffectiveExpiry(id: String, effectiveExpiryDate: Long) = error("not used")
}

private class FakeSensorRepository : SensorRepository {
    override fun observeConfigs(): Flow<List<SensorConfigEntity>> = flowOf(emptyList())
    override fun observeConfigById(id: String): Flow<SensorConfigEntity?> = flowOf(null)
    override fun observeLatestPerSensor(): Flow<List<SensorReadingEntity>> = flowOf(emptyList())
    override fun observeReadingsForSensorSince(sensorId: String, since: Long): Flow<List<SensorReadingEntity>> =
        flowOf(emptyList())

    override suspend fun upsertConfig(config: SensorConfigEntity) = error("not used")
    override suspend fun deleteConfig(id: String) = error("not used")
    override suspend fun insertReading(reading: SensorReadingEntity) = error("not used")
    override suspend fun pruneReadingsOlderThan(cutoff: Long) = error("not used")
    override suspend fun getReadingsForSensorSince(sensorId: String, since: Long): List<SensorReadingEntity> =
        error("not used")
}
