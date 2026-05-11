package com.lab03g10.smartpantry.data.sensor

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * @(#) SensorSimulatorTest.kt 1.0 2026-05-11
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
class SensorSimulatorTest {

    // Cursor tests never call context.assets, so a bare Mockito stub is enough
    private fun newSimulator(): SensorSimulator = SensorSimulator(mock(Context::class.java))

    @Test
    fun `parses well-formed CSV correctly`() {
        val csv = """
            epochSec,tempC,humidity,lux
            1767139200,4.2,42.5,0.0
            1767140100,8.5,45.0,250.0
        """.trimIndent()

        val rows = SensorSimulator.parseCsv(csv.reader().buffered())

        assertEquals(2, rows.size)
        assertEquals(4.2f, rows[0].tempC, 0.001f)
        assertEquals(8.5f, rows[1].tempC, 0.001f)
        assertEquals(250.0f, rows[1].lux, 0.001f)
    }

    @Test
    fun `nextReading advances cursor and wraps around`() {
        val sim = newSimulator()
        val rows = listOf(
            CsvRow(epochSec = 1L, tempC = 1f, humidity = 40f, lux = 0f),
            CsvRow(epochSec = 2L, tempC = 2f, humidity = 41f, lux = 0f),
            CsvRow(epochSec = 3L, tempC = 3f, humidity = 42f, lux = 0f),
        )
        sim.loadFromRows("fridge", rows)

        val r1 = sim.nextReading("fridge", 100L)!!
        val r2 = sim.nextReading("fridge", 200L)!!
        val r3 = sim.nextReading("fridge", 300L)!!
        val r4 = sim.nextReading("fridge", 400L)!!

        assertEquals(1f, r1.temperatureC, 0.001f)
        assertEquals(2f, r2.temperatureC, 0.001f)
        assertEquals(3f, r3.temperatureC, 0.001f)
        assertEquals(1f, r4.temperatureC, 0.001f)
    }

    @Test
    fun `nextReading returns null before load`() {
        val sim = newSimulator()
        assertNull(sim.nextReading("never-loaded", 0L))
    }

    @Test
    fun `timestamp uses nowMillis, not csv epochSec`() {
        val sim = newSimulator()
        sim.loadFromRows(
            sensorId = "fridge",
            rows = listOf(CsvRow(epochSec = 1L, tempC = 4f, humidity = 40f, lux = 0f)),
        )

        val reading = sim.nextReading("fridge", nowMillis = 999L)!!

        assertEquals(999L, reading.timestamp)
        assertNotEquals(1L, reading.timestamp)
        assertNotEquals(1000L, reading.timestamp)
    }
}
