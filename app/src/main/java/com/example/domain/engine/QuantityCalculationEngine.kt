package com.example.domain.engine

import com.example.data.models.Opening

data class CalculatedRoomMetrics(
    val floorArea: Double,
    val ceilingArea: Double,
    val rawPerimeter: Double,
    val perimeter: Double,
    val wallArea: Double,
    val openingsArea: Double,
    val netWallArea: Double
)

object QuantityCalculationEngine {

    fun calculateMetrics(
        length: Double,
        width: Double,
        height: Double,
        openings: List<Opening>
    ): CalculatedRoomMetrics {
        val floorArea = length * width
        val ceilingArea = length * width

        val totalDoorsWidth = openings.filter { it.type == "DOOR" }.sumOf { it.width * it.count }
        val rawPerimeter = 2 * (length + width)
        val perimeter = if (rawPerimeter - totalDoorsWidth < 0.0) 0.0 else rawPerimeter - totalDoorsWidth

        val wallArea = 2 * (length + width) * height
        val doorsArea = openings.filter { it.type == "DOOR" }.sumOf { it.totalArea }
        val windowsArea = openings.filter { it.type == "WINDOW" }.sumOf { it.totalArea }
        val openingsArea = doorsArea + windowsArea
        val netWallArea = if (wallArea - openingsArea < 0.0) 0.0 else wallArea - openingsArea

        return CalculatedRoomMetrics(
            floorArea = floorArea,
            ceilingArea = ceilingArea,
            rawPerimeter = rawPerimeter,
            perimeter = perimeter,
            wallArea = wallArea,
            openingsArea = openingsArea,
            netWallArea = netWallArea
        )
    }
}
