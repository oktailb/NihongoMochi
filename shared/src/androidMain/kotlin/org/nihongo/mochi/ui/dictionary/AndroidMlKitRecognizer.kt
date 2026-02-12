package org.nihongo.mochi.ui.dictionary

import android.util.Log
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.nihongo.mochi.domain.recognition.HandwritingRecognizer
import org.nihongo.mochi.domain.recognition.ModelStatus
import org.nihongo.mochi.domain.recognition.RecognitionStroke

class AndroidMlKitRecognizer : HandwritingRecognizer {

    private val _modelStatus = MutableStateFlow(ModelStatus.NOT_DOWNLOADED)
    override val modelStatus: StateFlow<ModelStatus> = _modelStatus.asStateFlow()

    private var recognizer: DigitalInkRecognizer? = null

    override fun downloadModel() {
        if (_modelStatus.value == ModelStatus.DOWNLOADED || _modelStatus.value == ModelStatus.DOWNLOADING) return
        _modelStatus.value = ModelStatus.DOWNLOADING

        val modelIdentifier = try {
            DigitalInkRecognitionModelIdentifier.fromLanguageTag("ja")
        } catch (e: MlKitException) {
            Log.e("MlKitRecognizer", "Failed to get model identifier", e)
            _modelStatus.value = ModelStatus.FAILED
            return
        }

        val model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()
        val remoteModelManager = RemoteModelManager.getInstance()

        remoteModelManager.download(model, DownloadConditions.Builder().build())
            .addOnSuccessListener {
                Log.i("MlKitRecognizer", "Model downloaded.")
                initializeRecognizer()
            }
            .addOnFailureListener { e ->
                Log.e("MlKitRecognizer", "Model download failed", e)
                _modelStatus.value = ModelStatus.FAILED
            }
    }

    private fun initializeRecognizer() {
        val modelIdentifier = try {
            DigitalInkRecognitionModelIdentifier.fromLanguageTag("ja")
        } catch (e: MlKitException) {
            _modelStatus.value = ModelStatus.FAILED
            return
        }
        val model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()
        val options = DigitalInkRecognizerOptions.builder(model).build()
        recognizer = DigitalInkRecognition.getClient(options)
        _modelStatus.value = ModelStatus.DOWNLOADED
        Log.i("MlKitRecognizer", "Recognizer initialized.")
    }

    override fun recognize(strokes: List<RecognitionStroke>, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {
        if (!isInitialized()) {
             // Try to initialize if possible (e.g. if downloaded but process restarted)
             // For now just fail or auto-init
             initializeRecognizer()
             if (!isInitialized()) {
                 onFailure(IllegalStateException("Recognizer not initialized"))
                 return
             }
        }

        val builder = Ink.builder()
        for (stroke in strokes) {
            val inkStrokeBuilder = Ink.Stroke.builder()
            for (point in stroke.points) {
                inkStrokeBuilder.addPoint(Ink.Point.create(point.x, point.y, point.t))
            }
            builder.addStroke(inkStrokeBuilder.build())
        }
        val ink = builder.build()

        recognizer!!.recognize(ink)
            .addOnSuccessListener { result ->
                val candidates = result.candidates.map { it.text }
                onSuccess(candidates)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    override fun isInitialized(): Boolean = recognizer != null
}