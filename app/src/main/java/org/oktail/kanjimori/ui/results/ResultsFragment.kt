package org.oktail.kanjimori.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oktail.kanjimori.databinding.FragmentResultsBinding

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    // Data variables
    private var recognitionProgress = 0
    private var recognitionTotal = 0
    private var readingProgress = 0
    private var readingTotal = 0
    private var writingProgress = 0
    private var writingTotal = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        updateUI()

        return root
    }

    private fun updateUI() {
        // Recognition
        binding.progressRecognition.progress = recognitionProgress
        binding.textRecognitionProgress.text = "$recognitionProgress% de maîtrise"
        binding.textRecognitionTotal.text = "Total: $recognitionTotal"

        // Reading
        binding.progressReading.progress = readingProgress
        binding.textReadingProgress.text = "$readingProgress% de maîtrise"
        binding.textReadingTotal.text = "Total: $readingTotal"

        // Writing
        binding.progressWritingResults.progress = writingProgress
        binding.textWritingProgress.text = "$writingProgress% de maîtrise"
        binding.textWritingTotal.text = "Total: $writingTotal"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}