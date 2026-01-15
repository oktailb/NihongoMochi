package org.nihongo.mochi.domain.levels

import kotlinx.serialization.Serializable
import org.nihongo.mochi.domain.statistics.StatisticsType

@Serializable
data class ActivityConfig(
    val dataFile: String,
    val enabled: Boolean = false,
    val jlpt: String? = null,
    val minRank: Int? = null,
    val maxRank: Int? = null
)

@Serializable
data class LevelDefinition(
    val id: String,
    val name: String,
    val description: String = "",
    val sortOrder: Int = 0,
    val globalStep: Int = 0,
    val dependencies: List<String> = emptyList(),
    val activities: Map<StatisticsType, ActivityConfig> = emptyMap()
)

@Serializable
data class SectionDefinition(
    val name: String,
    val description: String = "",
    val prerequisiteFor: List<String> = emptyList(),
    val sortOrder: Int = 0,
    val levels: List<LevelDefinition> = emptyList()
)

@Serializable
data class LevelDefinitions(
    val version: String = "1.0",
    val sections: Map<String, SectionDefinition> = emptyMap(),
    val activityTypes: Map<String, String> = emptyMap()
)
