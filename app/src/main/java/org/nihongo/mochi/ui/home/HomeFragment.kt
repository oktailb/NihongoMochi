package org.nihongo.mochi.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonRecognition.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_recognition)
        }

        binding.buttonReading.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_reading)
        }

        binding.buttonWriting.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_writing)
        }

        binding.buttonDictionary.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_dictionary)
        }

        binding.buttonOptions.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_options)
        }

        binding.buttonResults.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_results)
        }

        binding.buttonAbout.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_about)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}