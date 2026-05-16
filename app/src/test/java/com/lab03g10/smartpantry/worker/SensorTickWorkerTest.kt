package com.lab03g10.smartpantry.worker

import com.lab03g10.smartpantry.data.local.entity.SensorConfigEntity
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import junit.framework.TestCase.assertEquals
import org.junit.Test


/**
 * @(#) SensorTickWorkerTest.kt 1.0 2026-05-13
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
class SensorTickWorkerTest {

    private val fridge = SensorConfigEntity(
        id = "f1",
        displayName = "Fridge",
        deviceId = "dev-1",
        environmentType = "REFRIGERATOR",
        thresholdC = 7.0f,
        maxLux = 50.0f,
        csvAssetName = "fridge.csv",
        pairedAt = 0L,
        active = true,
    )

    private fun reading(temperatureC: Float, humidity: Float, lux: Float) = SensorReadingEntity(
        id = 0L,
        sensorId = fridge.id,
        timestamp = 0L,
        temperatureC = temperatureC,
        humidity = humidity,
        lux = lux,
        breachFlag = 0,
    )

    @Test
    fun `clean reading returns flag 0`() {
        assertEquals(0, computeBreachFlag(reading(3.0f, 50f, 10f), fridge))
    }

    @Test fun `temperature above threshold sets bit 0`() {
        assertEquals(0b001, computeBreachFlag(reading(9.0f, 50f, 10f), fridge))
    }

    @Test fun `lux above threshold sets bit 2`() {
        assertEquals(0b100, computeBreachFlag(reading(3.0f, 50f, 200f), fridge))
    }

    @Test fun `both temp and lux breach sets bits 0 and 2`() {
        assertEquals(0b101, computeBreachFlag(reading(9.0f, 50f, 200f), fridge))
    }

    @Test fun `exact threshold value is not a breach`() {
        // strict >  comparison: 7.0 > 7.0 == false, 50.0 > 50.0 == false
        assertEquals(0, computeBreachFlag(reading(7.0f, 50f, 50f), fridge))
    }
}