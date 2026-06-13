package com.aistudio.smartareabill.domain.engine

import com.aistudio.smartareabill.data.models.RoomEntity
import com.aistudio.smartareabill.data.models.WorkItem

object AggregationEngine {

    /**
     * Aggregates total floor area across all rooms.
     */
    fun calculateTotalFloorArea(rooms: List<RoomEntity>): Double {
        return rooms.sumOf { it.floorArea }
    }

    /**
     * Aggregates total cost across all rooms.
     */
    fun calculateGrandTotal(rooms: List<RoomEntity>): Double {
        return rooms.sumOf { it.totalCost }
    }

    /**
     * Categorizes a work item based on its name to a standard construction category.
     */
    fun detectCategory(workItemName: String): String {
        return when {
            workItemName.contains("دهان") || workItemName.contains("طلاء") || workItemName.contains("صبغ") -> "🎨 أعمال الدهان والتشطيب"
            workItemName.contains("بلاط") || workItemName.contains("سيراميك") || workItemName.contains("أرض") || workItemName.contains("رخام") -> "🧱 أعمال الأرضيات والرخام"
            workItemName.contains("سقف") || workItemName.contains("أسقف") || workItemName.contains("جبس") -> "🪵 أعمال الأسقف والجبس بورد"
            workItemName.contains("باب") || workItemName.contains("أبواب") || workItemName.contains("نافذة") || workItemName.contains("نوافذ") -> "🚪 أعمال الأبواب والشبابيك"
            else -> "⚙️ أعمال عامة وتجهيزات"
        }
    }

    /**
     * Generates a structural breakdown of costs grouped by detected categories.
     */
    fun generateCategoryBreakdown(rooms: List<RoomEntity>): Map<String, Double> {
        val breakdown = mutableMapOf<String, Double>()
        for (room in rooms) {
            val metrics = QuantityCalculationEngine.calculateMetrics(
                room.length, room.width, room.height, room.openings
            )
            for (work in room.workItems) {
                val qty = QuantityEngine.calculateQuantity(work.quantityType, work.customQuantity, metrics, room.openings)
                val cost = PricingEngine.calculateCost(qty, work.unitPrice)
                val category = detectCategory(work.name)
                breakdown[category] = (breakdown[category] ?: 0.0) + cost
            }
        }
        return breakdown
    }
}
