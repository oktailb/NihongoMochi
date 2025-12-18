package org.oktail.kanjimori.ui.writing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.oktail.kanjimori.R
import org.oktail.kanjimori.databinding.FragmentWritingBinding

data class LevelInfo(val button: Button, val xmlName: String, val stringResId: Int)

class WritingFragment : Fragment() {

    private var _binding: FragmentWritingBinding? = null
    private val binding get() = _binding!!

    private lateinit var levelInfos: List<LevelInfo>

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

        initializeLevelInfos()
        setupClickListeners()
    }

    private fun initializeLevelInfos() {
        levelInfos = listOf(
            LevelInfo(binding.buttonWritingN5, "N5", R.string.level_n5),
            LevelInfo(binding.buttonWritingN4, "N4", R.string.level_n4),
            LevelInfo(binding.buttonWritingN3, "N3", R.string.level_n3),
            LevelInfo(binding.buttonWritingN2, "N2", R.string.level_n2),
            LevelInfo(binding.buttonWritingN1, "N1", R.string.level_n1),
            LevelInfo(binding.buttonWritingClass1, "Grade 1", R.string.level_class_1),
            LevelInfo(binding.buttonWritingClass2, "Grade 2", R.string.level_class_2),
            LevelInfo(binding.buttonWritingClass3, "Grade 3", R.string.level_class_3),
            LevelInfo(binding.buttonWritingClass4, "Grade 4", R.string.level_class_4),
            LevelInfo(binding.buttonWritingClass5, "Grade 5", R.string.level_class_5),
            LevelInfo(binding.buttonWritingClass6, "Grade 6", R.string.level_class_6),
            LevelInfo(binding.buttonWritingTest4, "Grade 7", R.string.level_high_school_1),
            LevelInfo(binding.buttonWritingTest3, "Grade 8", R.string.level_high_school_2),
            LevelInfo(binding.buttonWritingTestPre2, "Grade 9", R.string.level_high_school_3),
            LevelInfo(binding.buttonWritingTest2, "Grade 10", R.string.level_high_school_4),
        )
    }

    private fun setupClickListeners() {
        for (info in levelInfos) {
            info.button.setOnClickListener { navigateToRecap(info.xmlName) }
        }
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