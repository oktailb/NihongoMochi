package org.nihongo.mochi.ui.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import org.nihongo.mochi.databinding.DialogDrawingBinding

class DrawingDialogFragment : DialogFragment() {

    private var _binding: DialogDrawingBinding? = null
    private val binding get() = _binding!!

    // Use the shared DictionaryViewModel
    private val viewModel: DictionaryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDrawingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonClear.setOnClickListener {
            binding.drawingView.clear()
        }

        binding.buttonRecognize.setOnClickListener {
            viewModel.recognizeInk(binding.drawingView.getInk())
        }

        viewModel.modelStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                ModelStatus.DOWNLOADING -> {
                    binding.textModelStatus.visibility = View.VISIBLE
                    binding.progressModelDownload.visibility = View.VISIBLE
                    binding.drawingView.visibility = View.GONE
                    binding.buttonRecognize.isEnabled = false
                }
                ModelStatus.DOWNLOADED -> {
                    binding.textModelStatus.visibility = View.GONE
                    binding.progressModelDownload.visibility = View.GONE
                    binding.drawingView.visibility = View.VISIBLE
                    binding.buttonRecognize.isEnabled = true
                }
                ModelStatus.FAILED -> {
                    binding.textModelStatus.text = "Model download failed."
                    binding.progressModelDownload.visibility = View.GONE
                    binding.buttonRecognize.isEnabled = false
                }
                else -> { /* Not Downloaded */ }
            }
        }

        viewModel.recognitionResults.observe(viewLifecycleOwner) { results ->
             // When results are in, dismiss the dialog. The parent fragment will handle the filtering.
            if (results != null) {
                dismiss()
                viewModel.clearRecognitionResults() // Reset for next time
            }
        }

        // Trigger download if needed
        if (viewModel.modelStatus.value == ModelStatus.NOT_DOWNLOADED) {
            viewModel.downloadModel()
        } else if (viewModel.modelStatus.value == ModelStatus.DOWNLOADED && !viewModel.isRecognizerInitialized()) {
            viewModel.initializeRecognizer()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}