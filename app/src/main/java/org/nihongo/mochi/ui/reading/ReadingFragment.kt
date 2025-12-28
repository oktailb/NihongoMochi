package org.nihongo.mochi.ui.reading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentReadingBinding
import org.nihongo.mochi.domain.statistics.ReadingViewModel

class ReadingFragment : Fragment() {

    private var _binding: FragmentReadingBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ReadingViewModel by viewModels {
        viewModelFactory {
            initializer {
                ReadingViewModel()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Ensure data is fresh when view is created
        viewModel.calculatePercentages()
        updateButtonText()
        setupClickListeners()
    }
    
    override fun onResume() {
        super.onResume()
        // Recalculate on resume in case user played a game and improved score
        viewModel.calculatePercentages()
        updateButtonText()
    }

    private fun updateButtonText() {
        binding.buttonReadingN5.text = "N5\n${viewModel.n5Percentage.toInt()}%"
        binding.buttonReadingN4.text = "N4\n${viewModel.n4Percentage.toInt()}%"
        binding.buttonReadingN3.text = "N3\n${viewModel.n3Percentage.toInt()}%"
        binding.buttonReadingN2.text = "N2\n${viewModel.n2Percentage.toInt()}%"
        binding.buttonReadingN1.text = "N1\n${viewModel.n1Percentage.toInt()}%"
        binding.buttonWords1000.text = "1000\n${viewModel.words1000Percentage.toInt()}%"
        binding.buttonWords2000.text = "2000\n${viewModel.words2000Percentage.toInt()}%"
        binding.buttonWords3000.text = "3000\n${viewModel.words3000Percentage.toInt()}%"
        binding.buttonWords4000.text = "4000\n${viewModel.words4000Percentage.toInt()}%"
        binding.buttonWords5000.text = "5000\n${viewModel.words5000Percentage.toInt()}%"
        binding.buttonWords6000.text = "6000\n${viewModel.words6000Percentage.toInt()}%"
        binding.buttonWords7000.text = "7000\n${viewModel.words7000Percentage.toInt()}%"
        binding.buttonWords8000.text = "8000\n${viewModel.words8000Percentage.toInt()}%"
        val userListText = getString(R.string.reading_user_list)
        binding.buttonUserList.text = "$userListText\n${viewModel.userListPercentage.toInt()}%"
    }

    private fun setupClickListeners() {
        binding.buttonReadingN5.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("jlpt_wordlist_n5")
            findNavController().navigate(action)
        }
        binding.buttonReadingN4.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("jlpt_wordlist_n4")
            findNavController().navigate(action)
        }
        binding.buttonReadingN3.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("jlpt_wordlist_n3")
            findNavController().navigate(action)
        }
        binding.buttonReadingN2.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("jlpt_wordlist_n2")
            findNavController().navigate(action)
        }
        binding.buttonReadingN1.setOnClickListener {
            val action = ReadingFragmentDirections.actionNavReadingToWordList("jlpt_wordlist_n1")
            findNavController().navigate(action)
        }
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
