package org.nihongo.mochi.presentation.reading

import org.nihongo.mochi.presentation.models.ReadingLevelInfoState

data class ReadingCategory(
    val name: String,
    val levels: List<ReadingLevelInfoState>
)
