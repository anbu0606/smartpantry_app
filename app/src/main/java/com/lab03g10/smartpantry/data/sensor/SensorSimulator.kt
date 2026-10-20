package com.lab03g10.smartpantry.data.sensor

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.lab03g10.smartpantry.data.local.entity.SensorReadingEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @(#) SensorSimulator.kt 1.0 2026-05-11
 * Copyright (c) 2026 the SmartPantry team.
 */
@Singleton
class SensorSimulator @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val cache: MutableMap<String, SensorSeries> = mutableMapOf()

    suspend fun load(sensorId: String, csvAssetName: String) {
        if (sensorId in cache) return
        val rows = withContext(Dispatchers.IO) {
            val stream = try {
                context.assets.open("sensors/$csvAssetName")
            } catch (e: FileNotFoundException) {
                throw SensorSimulatorException("Asset sensors/$csvAssetName not found", e)
            }
            stream.bufferedReader().use { parseCsv(it) }
        }
        if (rows.isEmpty()) {
            throw SensorSimulatorException("CSV $csvAssetName has no data rows")
        }
        cache[sensorId] = SensorSeries(rows = rows)
    }

    fun nextReading(
        sensorId: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): SensorReadingEntity? {
        val series = cache[sensorId] ?: return null
        if (series.rows.isEmpty()) return null

        val row = series.rows[series.cursor]
        series.cursor = (series.cursor + 1) % series.rows.size

        return SensorReadingEntity(
            id = 0,
            sensorId = sensorId,
            // Real "observed" time, NOT row.epochSec — CSV epoch is data shape only
            timestamp = nowMillis,
            temperatureC = row.tempC,
            humidity = row.humidity,
            lux = row.lux,
            // ThresholdEngine sets breach bits later; raw simulator emits 0
            breachFlag = 0,
        )
    }

    @VisibleForTesting
    internal fun loadFromRows(sensorId: String, rows: List<CsvRow>) {
        if (sensorId in cache) return
        cache[sensorId] = SensorSeries(rows = rows)
    }

    private data class SensorSeries(
        val rows: List<CsvRow>,
        var cursor: Int = 0,
    )

    companion object {
        @VisibleForTesting
        internal fun parseCsv(reader: BufferedReader): List<CsvRow> =
            reader.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .drop(1)
                .map(::parseRow)
                .toList()

        private fun parseRow(line: String): CsvRow {
            val parts = line.split(",")
            if (parts.size != 4) {
                throw SensorSimulatorException("Malformed row (expected 4 columns): $line")
            }
            return try {
                CsvRow(
                    epochSec = parts[0].toLong(),
                    tempC = parts[1].toFloat(),
                    humidity = parts[2].toFloat(),
                    lux = parts[3].toFloat(),
                )
            } catch (e: NumberFormatException) {
                throw SensorSimulatorException("Unparseable row: $line", e)
            }
        }
    }
}

internal data class CsvRow(
    val epochSec: Long,
    val tempC: Float,
    val humidity: Float,
    val lux: Float,
)

class SensorSimulatorException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
