package com.example.domain.engine

import com.example.data.models.Opening

object QuantityEngine {

    fun calculateQuantity(
        quantityType: String,
        customQuantity: Double,
        metrics: CalculatedRoomMetrics,
        openings: List<Opening>
    ): Double {
        return when (quantityType) {
            "WALL_NET" -> metrics.netWallArea
            "FLOOR" -> {
                val totalDoorWidth = openings.filter { it.type == "DOOR" }.sumOf { it.width * it.count }
                val netFloor = metrics.floorArea - totalDoorWidth
                if (netFloor < 0.0) 0.0 else netFloor
            }
            "CEILING" -> {
                val netCeiling = metrics.ceilingArea
                if (netCeiling < 0.0) 0.0 else netCeiling
            }
            "PERIMETER" -> metrics.perimeter
            "DOORS_COUNT" -> openings.filter { it.type == "DOOR" }.sumOf { it.count }.toDouble()
            "WINDOWS_COUNT" -> openings.filter { it.type == "WINDOW" }.sumOf { it.count }.toDouble()
            "CUSTOM" -> customQuantity
            else -> 0.0
        }
    }
}
