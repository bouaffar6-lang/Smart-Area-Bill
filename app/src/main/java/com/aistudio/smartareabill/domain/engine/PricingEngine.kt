package com.aistudio.smartareabill.domain.engine

object PricingEngine {

    /**
     * Calculates the cost of a single work item based on its quantity and unit price.
     */
    fun calculateCost(quantity: Double, unitPrice: Double): Double {
        if (quantity < 0.0 || unitPrice < 0.0) return 0.0
        return quantity * unitPrice
    }

    /**
     * Calculates the total cost of a group of calculated items.
     */
    fun calculateTotal(itemCosts: List<Double>): Double {
        return itemCosts.sumOf { if (it > 0.0) it else 0.0 }
    }

    /**
     * Calculates the subtotal cost applying a margin or discount.
     */
    fun applyAdjustment(baseTotal: Double, percentage: Double, isMarkup: Boolean): Double {
        val multiplier = if (isMarkup) (1.0 + percentage / 100.0) else (1.0 - percentage / 100.0)
        val adjusted = baseTotal * multiplier
        return if (adjusted < 0.0) 0.0 else adjusted
    }
}
