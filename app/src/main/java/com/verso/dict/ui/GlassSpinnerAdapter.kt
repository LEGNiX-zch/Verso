package com.verso.dict.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.verso.dict.R
import com.verso.dict.util.FontSizeManager

/**
 * Custom spinner adapter that shows a liquid-glass blue selection ring around the
 * currently-selected item in the dropdown, so the user can always tell which card
 * set is active. The collapsed view uses [item_spinner_glass]; the dropdown uses
 * [item_spinner_dropdown_glass] with a switchable background.
 */
class GlassSpinnerAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    /** Position of the currently-selected item, or -1 if none. */
    var selectedPosition: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_spinner_glass, parent, false)
        val text = view.findViewById<TextView>(R.id.tv_spinner_item)
        text.text = items.getOrNull(position).orEmpty()
        FontSizeManager.applyScaled(context, text, R.dimen.definition_text)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_spinner_dropdown_glass, parent, false)

        val container = view.findViewById<android.widget.LinearLayout>(R.id.ll_dropdown_item)
        val text = view.findViewById<TextView>(R.id.tv_dropdown_item)
        val check = view.findViewById<ImageView>(R.id.iv_dropdown_check)

        text.text = items.getOrNull(position).orEmpty()
        FontSizeManager.applyScaled(context, text, R.dimen.definition_text)

        // Blue selection ring on the active item; plain glass on others.
        if (position == selectedPosition) {
            container.setBackgroundResource(R.drawable.bg_glass_selected)
            check.visibility = View.VISIBLE
            text.setTextColor(context.getColor(R.color.glass_selected_stroke))
            text.setTypeface(text.typeface, android.graphics.Typeface.BOLD)
        } else {
            container.setBackgroundResource(R.drawable.bg_glass_sm)
            check.visibility = View.GONE
            text.setTextColor(context.getColor(R.color.text_primary))
            text.setTypeface(text.typeface, android.graphics.Typeface.NORMAL)
        }
        return view
    }
}
