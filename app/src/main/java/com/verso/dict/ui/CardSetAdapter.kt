package com.verso.dict.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.verso.dict.R
import com.verso.dict.data.CardSet
import com.verso.dict.util.FontSizeManager

/** Adapter for the card-set list inside Settings. Fonts follow the global setting. */
class CardSetAdapter(
    private val onDelete: (CardSet) -> Unit
) : ListAdapter<CardSet, CardSetAdapter.SetVH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_set, parent, false)
        return SetVH(view)
    }

    override fun onBindViewHolder(holder: SetVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SetVH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.tv_set_name)
        private val btnDelete: android.widget.Button = itemView.findViewById(R.id.btn_delete_set)

        fun bind(set: CardSet) {
            nameText.text = set.name
            FontSizeManager.applyScaled(itemView.context, nameText, R.dimen.word_text)
            btnDelete.setOnClickListener { onDelete(set) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CardSet>() {
            override fun areItemsTheSame(a: CardSet, b: CardSet) = a.id == b.id
            override fun areContentsTheSame(a: CardSet, b: CardSet) = a == b
        }
    }
}
