package org.nihongo.mochi.ui.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import org.nihongo.mochi.R

data class LanguageItem(val code: String, val name: String, val flagResId: Int)

class LanguageAdapter(context: Context, languages: List<LanguageItem>) :
    ArrayAdapter<LanguageItem>(context, 0, languages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = createItemView(position, convertView, parent)
        view.setBackgroundColor(context.resources.getColor(R.color.leaf_green, null))
        return view
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_with_flag, parent, false)

        val item = getItem(position)

        val flagImageView = view.findViewById<ImageView>(R.id.image_flag)
        val nameTextView = view.findViewById<TextView>(R.id.text_language)

        if (item != null) {
            flagImageView.setImageResource(item.flagResId)
            nameTextView.text = item.name
        }

        return view
    }
}