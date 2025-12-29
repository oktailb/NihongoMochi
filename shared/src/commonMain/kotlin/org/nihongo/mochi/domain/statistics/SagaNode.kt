package org.nihongo.mochi.domain.statistics

data class SagaNode(
    val id: String, 
    val title: String,
    val recognitionId: String?,
    val readingId: String?,
    val writingId: String?,
    val mainType: StatisticsType = StatisticsType.RECOGNITION
)

data class SagaStep(
    val id: String, // Unique ID for the step (useful for DiffUtil)
    val nodes: List<SagaNode>
)

enum class SagaTab {
    JLPT,
    SCHOOL,
    CHALLENGES
}

data class UserSagaProgress(
    val recognitionIndex: Int,
    val readingIndex: Int,
    val writingIndex: Int,
    val nodeProgress: Map<String, Int> = emptyMap()
)
