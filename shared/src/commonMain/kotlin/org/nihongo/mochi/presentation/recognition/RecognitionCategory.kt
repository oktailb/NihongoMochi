package org.nihongo.mochi.presentation.recognition

import org.nihongo.mochi.presentation.models.LevelInfoState

data class RecognitionCategory(
    val name: String,
    val levels: List<LevelInfoState>
)
