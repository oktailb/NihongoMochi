package org.oktail.kanjimori.ui.reading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.oktail.kanjimori.R
import org.oktail.kanjimori.databinding.FragmentReadingBinding
import org.oktail.kanjimori.data.ScoreManager

class ReadingFragment : Fragment() {

    private var _binding: FragmentReadingBinding? = null
    private val binding get() = _binding!!

    // Percentage variables
    private var n5Percentage = 0.0
    private var n4Percentage = 0.0
    private var n3Percentage = 0.0
    private var n2Percentage = 0.0
    private var n1Percentage = 0.0
    private var words1000Percentage = 0.0
    private var words2000Percentage = 0.0
    private var words3000Percentage = 0.0
    private var words4000Percentage = 0.0
    private var words5000Percentage = 0.0
    private var words6000Percentage = 0.0
    private var words7000Percentage = 0.0
    private var words8000Percentage = 0.0
    private var userListPercentage = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Recalculate percentages here or in onResume to ensure they are up to date
        calculateUserListPercentage()
        updateButtonText()
        setupClickListeners()
    }
    
    override fun onResume() {
        super.onResume()
        calculateUserListPercentage()
        updateButtonText()
    }

    private fun calculateUserListPercentage() {
        val scores = ScoreManager.getAllScores(requireContext())
        if (scores.isEmpty()) {
            userListPercentage = 0.0
            return
        }
        // Percentage based on encountered words that are mastered?
        // Or simple progress? 
        // Given the requirement: "user list" contains words encountered but not mastered.
        // Let's display the percentage of mastered words among all encountered words.
        val totalEncountered = scores.size
        val mastered = scores.count { (_, score) -> (score.successes - score.failures) >= 10 }
        
        userListPercentage = if (totalEncountered > 0) {
            (mastered.toDouble() / totalEncountered.toDouble()) * 100.0
        } else {
            0.0
        }
    }

    private fun updateButtonText() {
        binding.buttonReadingN5.text = "N5\n${n5Percentage.toInt()}%"
        binding.buttonReadingN4.text = "N4\n${n4Percentage.toInt()}%"
        binding.buttonReadingN3.text = "N3\n${n3Percentage.toInt()}%"
        binding.buttonReadingN2.text = "N2\n${n2Percentage.toInt()}%"
        binding.buttonReadingN1.text = "N1\n${n1Percentage.toInt()}%"
        binding.buttonWords1000.text = "1000\n${words1000Percentage.toInt()}%"
        binding.buttonWords2000.text = "2000\n${words2000Percentage.toInt()}%"
        binding.buttonWords3000.text = "3000\n${words3000Percentage.toInt()}%"
        binding.buttonWords4000.text = "4000\n${words4000Percentage.toInt()}%"
        binding.buttonWords5000.text = "5000\n${words5000Percentage.toInt()}%"
        binding.buttonWords6000.text = "6000\n${words6000Percentage.toInt()}%"
        binding.buttonWords7000.text = "7000\n${words7000Percentage.toInt()}%"
        binding.buttonWords8000.text = "8000\n${words8000Percentage.toInt()}%"
        val userListText = getString(R.string.reading_user_list)
        binding.buttonUserList.text = "$userListText\n${userListPercentage.toInt()}%"
    }

    private fun setupClickListeners() {
        binding.buttonWords1000.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("bccwj_wordlist_1000")
            findNavController().navigate(action)
        }
        binding.buttonWords2000.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("bccwj_wordlist_2000")
            findNavController().navigate(action)
        }
        binding.buttonWords3000.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("bccwj_wordlist_3000")
            findNavController().navigate(action)
        }
        binding.buttonWords4000.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("bccwj_wordlist_4000")
            findNavController().navigate(action)
        }
        binding.buttonWords5000.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("bccwj_wordlist_5000")
            findNavController().navigate(action)
        }
        binding.buttonWords6000.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("bccwj_wordlist_6000")
            findNavController().navigate(action)
        }
        binding.buttonWords7000.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("bccwj_wordlist_7000")
            findNavController().navigate(action)
        }
        binding.buttonWords8000.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("bccwj_wordlist_8000")
            findNavController().navigate(action)
        }
        binding.buttonUserList.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("user_custom_list")
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}