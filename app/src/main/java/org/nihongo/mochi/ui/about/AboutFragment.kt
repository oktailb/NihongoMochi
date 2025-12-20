package org.nihongo.mochi.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.nihongo.mochi.databinding.FragmentAboutBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.nihongo.mochi.BuildConfig

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
        binding.textVersion.text = BuildConfig.VERSION_NAME

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutIssueTracker.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/oktailb/KanjiMori/issues"))
            startActivity(intent)
        }

        binding.layoutPatreon.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.patreon.com/Oktail"))
            startActivity(intent)
        }

        binding.layoutTipeee.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://en.tipeee.com/lecoq-vincent"))
            startActivity(intent)
        }

        binding.layoutKanjiData.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/davidluzgouveia/kanji-data"))
            startActivity(intent)
        }

        binding.layoutHeaderPicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.flickr.com/photos/theknowlesgallery/"))
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}