package org.oktail.kanjimori.ui.writing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.oktail.kanjimori.R
import org.oktail.kanjimori.databinding.FragmentWritingBinding

class WritingFragment : Fragment() {

    private var _binding: FragmentWritingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWritingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.buttonWritingN5.setOnClickListener { navigateToRecap("N5") }
        binding.buttonWritingN4.setOnClickListener { navigateToRecap("N4") }
        binding.buttonWritingN3.setOnClickListener { navigateToRecap("N3") }
        binding.buttonWritingN2.setOnClickListener { navigateToRecap("N2") }
        binding.buttonWritingN1.setOnClickListener { navigateToRecap("N1") }

        binding.buttonWritingClass1.setOnClickListener { navigateToRecap("Grade 1") }
        binding.buttonWritingClass2.setOnClickListener { navigateToRecap("Grade 2") }
        binding.buttonWritingClass3.setOnClickListener { navigateToRecap("Grade 3") }
        binding.buttonWritingClass4.setOnClickListener { navigateToRecap("Grade 4") }
        binding.buttonWritingClass5.setOnClickListener { navigateToRecap("Grade 5") }
        binding.buttonWritingClass6.setOnClickListener { navigateToRecap("Grade 6") }

        // Assuming these buttons correspond to Kanji Kentei levels.
        // Note: "Kanken Pre-2" is not defined in your kanji_levels.xml, so no listener is set for it.
        binding.buttonWritingTest4.setOnClickListener { navigateToRecap("Level 4") }
        binding.buttonWritingTest3.setOnClickListener { navigateToRecap("Level 3") }
        binding.buttonWritingTest2.setOnClickListener { navigateToRecap("Level 2") }
    }

    private fun navigateToRecap(level: String) {
        val action = WritingFragmentDirections.actionNavWritingToWritingRecap(level)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}