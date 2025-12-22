package org.nihongo.mochi.ui.dictionary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink
import kotlinx.coroutines.launch
import org.nihongo.mochi.ui.dictionary.DictionaryFragment.DictionaryItem

enum class ModelStatus { NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED, FAILED }
enum class SearchMode { READING, MEANING }

class DictionaryViewModel : ViewModel() {

    // --- Dictionary Data ---
    var isDataLoaded = false
    val allKanjiList = mutableListOf<DictionaryItem>()
    val kanjiDataMap = mutableMapOf<String, DictionaryItem>()
    var lastResults: List<DictionaryItem> = emptyList()

    // --- Filter State ---
    var searchMode: SearchMode = SearchMode.READING
    var textQuery: String = ""
    var strokeQuery: String = ""
    var exactMatch: Boolean = false
    var drawingCandidates: List<String>? = null

    // --- Drawing Recognition State ---
    private val _modelStatus = MutableLiveData<ModelStatus>(ModelStatus.NOT_DOWNLOADED)
    val modelStatus: LiveData<ModelStatus> = _modelStatus

    private val _recognitionResults = MutableLiveData<List<String>?>()
    val recognitionResults: LiveData<List<String>?> = _recognitionResults
    
    var lastInk: Ink? = null

    private var recognizer: DigitalInkRecognizer? = null

    fun isRecognizerInitialized(): Boolean = recognizer != null

    fun downloadModel() {
        if (_modelStatus.value == ModelStatus.DOWNLOADED || _modelStatus.value == ModelStatus.DOWNLOADING) return
        _modelStatus.value = ModelStatus.DOWNLOADING

        val modelIdentifier = try {
            DigitalInkRecognitionModelIdentifier.fromLanguageTag("ja")
        } catch (e: MlKitException) {
            Log.e("ViewModel", "Failed to get model identifier", e)
            _modelStatus.value = ModelStatus.FAILED
            return
        }

        val model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()
        val remoteModelManager = com.google.mlkit.common.model.RemoteModelManager.getInstance()

        remoteModelManager.download(model, com.google.mlkit.common.model.DownloadConditions.Builder().build())
            .addOnSuccessListener { 
                Log.i("ViewModel", "Model downloaded.")
                initializeRecognizer() 
            }
            .addOnFailureListener { e -> 
                Log.e("ViewModel", "Model download failed", e)
                _modelStatus.value = ModelStatus.FAILED
            }
    }

    fun initializeRecognizer() {
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
        Log.i("ViewModel", "Recognizer initialized.")
    }

    fun recognizeInk(ink: Ink) {
        if (!isRecognizerInitialized()) return
        lastInk = ink
        recognizer!!.recognize(ink)
            .addOnSuccessListener { result ->
                val candidates = result.candidates.map { it.text }
                drawingCandidates = candidates
                _recognitionResults.postValue(candidates)
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "Recognition failed", e)
                _recognitionResults.postValue(null)
            }
    }

    fun clearDrawingFilter() {
        drawingCandidates = null
        lastInk = null
    }

    fun clearRecognitionResults() {
        _recognitionResults.value = null
    }
}