package com.example.domain.engine

import android.graphics.Color

object PdfEngine {

    /**
     * Elegant palette color configurations for professional enterprise PDFs.
     */
    object Theme {
        val PRIMARY_NAVY = Color.parseColor("#1B2A4A")
        val SECONDARY_STEEL = Color.parseColor("#2C3E50")
        val LIGHT_BG = Color.parseColor("#F4F6FA")
        val LIGHT_GRAY_ROW = Color.parseColor("#FAFAFA")
        val BORDER_COLOR = Color.parseColor("#EEEEEE")
        val TEXT_DARK = Color.BLACK
        val TEXT_LIGHT = Color.WHITE
        val TEXT_MUTED = Color.GRAY
    }

    /**
     * Formats names and metrics professionally for the engineering BOQ PDF.
     */
    fun formatDimensionsArabic(length: Double, width: Double, height: Double): String {
        return "${String.format("%.1f", length)}م (طول) × ${String.format("%.1f", width)}م (عرض) × ${String.format("%.1f", height)}م (ارتفاع)"
    }

    fun formatAreaArabic(area: Double): String {
        return "${String.format("%.1f", area)} م²"
    }

    fun formatPerimeterArabic(perimeter: Double): String {
        return "${String.format("%.1f", perimeter)} م"
    }

    fun formatPriceArabic(price: Double): String {
        return "${String.format("%,.0f", price)} دج"
    }
}
