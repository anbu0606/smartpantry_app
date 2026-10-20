package com.lab03g10.smartpantry.data.sensor

import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import junit.framework.TestCase.assertEquals
import org.junit.Test


/**
 * @(#) ThresholdEngineTest.kt 1.0 2026-05-15
 * Copyright (c) 2026 the SmartPantry team.
 */
class ThresholdEngineTest {

    private val engine = ThresholdEngine()

    private val fridge = SensorConfigEntity(
        id = "f1",
        displayName = "Fridge",
        deviceId = "dev-1",
        environmentType = "REFRIGERATOR",
        thresholdC = 7.0f,
        humidityThreshold = 70.0f,
        maxLux = 50.0f,
        csvAssetName = "fridge.csv",
        pairedAt = 0L,
        active = true,
    )

    private fun reading(
        temperatureC: Float,
        lux: Float,
        timestamp: Long = 0L,
    ) = SensorReadingEntity(
        id = 0L,
        sensorId = fridge.id,
        timestamp = timestamp,
        temperatureC = temperatureC,
        humidity = 50f,
        lux = lux,
        breachFlag = 0,
    )

    @Test
    fun `empty readings returns zero summary`() {
        val result = engine.analyse(fridge, emptyList())
        assertEquals(0, result.breachCount)
        assertEquals(0, result.cleanCount)
        assertEquals(0, result.totalCount)
        assertEquals(0L, result.windowStartMs)
        assertEquals(0L, result.windowEndMs)
    }

    @Test
    fun `all clean readings`() {
        val readings = List(5) { reading(temperatureC = 3.0f, lux = 10f) }
        val result = engine.analyse(fridge, readings)
        assertEquals(0, result.breachCount)
        assertEquals(5, result.cleanCount)
        assertEquals(5, result.totalCount)
    }

    @Test
    fun `all breach readings - temperature`() {
        val readings = List(5) { reading(temperatureC = 9.0f, lux = 10f) }
        val result = engine.analyse(fridge, readings)
        assertEquals(5, result.breachCount)
        assertEquals(0, result.cleanCount)
        assertEquals(5, result.totalCount)
    }

    @Test
    fun `all breach readings - lux`() {
        val readings = List(5) { reading(temperatureC = 3.0f, lux = 100f) }
        val result = engine.analyse(fridge, readings)
        assertEquals(5, result.breachCount)
        assertEquals(0, result.cleanCount)
        assertEquals(5, result.totalCount)
    }

    @Test
    fun `mixed breach and clean`() {
        val readings = listOf(
            reading(temperatureC = 9.0f, lux = 10f),   // temp breach
            reading(temperatureC = 9.5f, lux = 10f),   // temp breach
            reading(temperatureC = 8.0f, lux = 10f),   // temp breach
            reading(temperatureC = 3.0f, lux = 10f),   // clean
            reading(temperatureC = 4.0f, lux = 10f),   // clean
        )
        val result = engine.analyse(fridge, readings)
        assertEquals(3, result.breachCount)
        assertEquals(2, result.cleanCount)
        assertEquals(5, result.totalCount)
    }

    @Test
    fun `exact threshold is not breach`() {
        // strict > comparison in analyse: 7.0 > 7.0 == false, 50.0 > 50.0 == false
        val readings = listOf(reading(temperatureC = 7.0f, lux = 50.0f))
        val result = engine.analyse(fridge, readings)
        assertEquals(0, result.breachCount)
        assertEquals(1, result.cleanCount)
    }

    @Test
    fun `window timestamps correct`() {
        val readings = listOf(
            reading(temperatureC = 3.0f, lux = 10f, timestamp = 100L),
            reading(temperatureC = 3.0f, lux = 10f, timestamp = 200L),
            reading(temperatureC = 3.0f, lux = 10f, timestamp = 300L),
        )
        val result = engine.analyse(fridge, readings)
        assertEquals(100L, result.windowStartMs)
        assertEquals(300L, result.windowEndMs)
        assertEquals(3, result.totalCount)
    }

    @Test
    fun `lux OR temp triggers breach`() {
        // temp clean (3.0 <= 7.0) but lux above (100 > 50) — OR semantics still counts as breach
        val readings = listOf(reading(temperatureC = 3.0f, lux = 100f))
        val result = engine.analyse(fridge, readings)
        assertEquals(1, result.breachCount)
        assertEquals(0, result.cleanCount)
    }
}