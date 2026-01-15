package org.nihongo.mochi.domain.recognition

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A no-op implementation of [HandwritingRecognizer] for platforms that don't support
 * handwriting recognition yet or for testing purposes.
 */
class NoOpHandwritingRecognizer : HandwritingRecognizer {
    private val _modelStatus = MutableStateFlow(ModelStatus.DOWNLOADED)
    override val modelStatus: StateFlow<ModelStatus> = _modelStatus.asStateFlow()

    override fun downloadModel() {
        // No-op: model is considered "ready" but won't do anything
    }

    override fun recognize(
        strokes: List<RecognitionStroke>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Always return an empty list of results successfully
        onSuccess(emptyList())
    }

    override fun isInitialized(): Boolean = true
}
