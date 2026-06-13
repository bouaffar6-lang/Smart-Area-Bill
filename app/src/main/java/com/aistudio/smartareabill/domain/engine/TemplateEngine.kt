package com.aistudio.smartareabill.domain.engine

import com.aistudio.smartareabill.data.models.WorkItemTemplate

object TemplateEngine {

    /**
     * Infers the correct quantity type and unit type for a template name.
     */
    fun inferQuantityTypeAndUnit(name: String): Pair<String, String> {
        val trimmed = name.trim()
        return when {
            trimmed.contains("باب") || trimmed.contains("أبواب") || trimmed.contains("الباب") -> {
                Pair("DOORS_COUNT", "piece")
            }
            trimmed.contains("نافذة") || trimmed.contains("نافدة") || trimmed.contains("النافذة") || trimmed.contains("النافدة") -> {
                Pair("WINDOWS_COUNT", "piece")
            }
            trimmed.contains("نعل") || trimmed.contains("نعلات") || trimmed.contains("المتر الطولي") || trimmed.contains("محيط") -> {
                Pair("PERIMETER", "m")
            }
            trimmed.contains("سقف") || trimmed.contains("أسقف") || trimmed.contains("جبس") -> {
                Pair("CEILING", "m²")
            }
            trimmed.contains("أرض") || trimmed.contains("سيراميك") || trimmed.contains("بلاط") || trimmed.contains("باركيه") -> {
                Pair("FLOOR", "m²")
            }
            else -> {
                Pair("WALL_NET", "m²")
            }
        }
    }

    /**
     * Standard built-in pre-set templates.
     */
    fun getPresetTemplates(): List<WorkItemTemplate> {
        return listOf(
            WorkItemTemplate("لياسة جدران (محارة)", 350.0),
            WorkItemTemplate("دهان جدران داخلي", 450.0),
            WorkItemTemplate("سيراميك أرضيات تفوق", 600.0),
            WorkItemTemplate("جبس بورد أسقف ديكور", 800.0),
            WorkItemTemplate("تركيب نعلات رخامية", 120.0),
            WorkItemTemplate("تركيب باب (بالحبة)", 4000.0),
            WorkItemTemplate("تركيب نافذة (بالحبة)", 6000.0),
            WorkItemTemplate("تركيب باب خشب فاخر", 4500.0),
            WorkItemTemplate("نافذة ألمنيوم قياسية", 7500.0)
        )
    }
}
