package org.nihongo.mochi.presentation.writing

import org.nihongo.mochi.presentation.models.WritingLevelInfoState

data class WritingCategory(
    val name: String,
    val levels: List<WritingLevelInfoState>
)
