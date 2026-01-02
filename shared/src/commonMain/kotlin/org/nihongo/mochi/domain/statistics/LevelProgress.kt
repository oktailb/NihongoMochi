package org.nihongo.mochi.domain.statistics

data class LevelProgress(
    val title: String, // Display name (e.g. "JLPT N5")
    val xmlName: String, // ID for content provider
    val percentage: Int,
    val type: StatisticsType, // Main section (Recognition, Reading...)
    val category: String, // Sub-section (JLPT, School, Frequency...)
    val sortOrder: Int = 0 // To order items within a category
)
