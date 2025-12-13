package org.oktail.kanjimori.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oktail.kanjimori.databinding.FragmentAboutBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sdf = SimpleDateFormat("dd MMM. yyyy HH:mm:ss", Locale.getDefault())
        val currentDate = sdf.format(Date())
        binding.textDate.text = currentDate

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}