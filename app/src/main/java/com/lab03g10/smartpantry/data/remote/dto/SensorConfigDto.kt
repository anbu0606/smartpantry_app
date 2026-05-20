package com.lab03g10.smartpantry.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity

/**
 * Firestore-shaped twin of SensorConfigEntity. Float fields are stored
 * as Double in Firestore — declare DTO doubles and convert at the
 * mapper boundary so deserialisation can't fail on type mismatch.
 *
 * SensorConfigEntity has no mutation timestamp; on pull the merge step
 * uses cloud-wins for matching ids so threshold edits made on a prior
 * device follow the user.
 */
data class SensorConfigDto(
    @get:PropertyName("id")                val id: String = "",
    @get:PropertyName("displayName")       val displayName: String = "",
    @get:PropertyName("deviceId")          val deviceId: String = "",
    @get:PropertyName("environmentType")   val environmentType: String = "",
    @get:PropertyName("thresholdC")        val thresholdC: Double = 0.0,
    @get:PropertyName("humidityThreshold") val humidityThreshold: Double = 0.0,
    @get:PropertyName("maxLux")            val maxLux: Double = 0.0,
    @get:PropertyName("csvAssetName")      val csvAssetName: String = "",
    @get:PropertyName("pairedAt")          val pairedAt: Long = 0L,
    @get:PropertyName("active")            val active: Boolean = true,
)

fun SensorConfigEntity.toDto() = SensorConfigDto(
    id = id,
    displayName = displayName,
    deviceId = deviceId,
    environmentType = environmentType,
    thresholdC = thresholdC.toDouble(),
    humidityThreshold = humidityThreshold.toDouble(),
    maxLux = maxLux.toDouble(),
    csvAssetName = csvAssetName,
    pairedAt = pairedAt,
    active = active,
)

fun SensorConfigDto.toEntity() = SensorConfigEntity(
    id = id,
    displayName = displayName,
    deviceId = deviceId,
    environmentType = environmentType,
    thresholdC = thresholdC.toFloat(),
    humidityThreshold = humidityThreshold.toFloat(),
    maxLux = maxLux.toFloat(),
    csvAssetName = csvAssetName,
    pairedAt = pairedAt,
    active = active,
)
