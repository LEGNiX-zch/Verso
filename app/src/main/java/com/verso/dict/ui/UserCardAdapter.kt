package com.verso.dict.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.verso.dict.R
import com.verso.dict.data.UserWord
import com.verso.dict.util.FontSizeManager

/** Adapter for the user card management list. Only ever shows user cards; built-in entries are
 *  invisible here. Fonts follow the global setting. */
class UserCardAdapter(
    private val onEdit: (UserWord) -> Unit,
    private val onDelete: (UserWord) -> Unit
) : ListAdapter<UserWord, UserCardAdapter.CardVH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_card, parent, false)
        return CardVH(view)
    }

    override fun onBindViewHolder(holder: CardVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CardVH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val wordText: TextView = itemView.findViewById(R.id.tv_word)
        private val defText: TextView = itemView.findViewById(R.id.tv_definition)
        private val btnEdit: android.widget.Button = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: android.widget.Button = itemView.findViewById(R.id.btn_delete)

        fun bind(card: UserWord) {
            wordText.text = card.word
            defText.text = card.definition
            FontSizeManager.applyScaled(itemView.context, wordText, R.dimen.word_text)
            FontSizeManager.applyScaled(itemView.context, defText, R.dimen.definition_text)
            btnEdit.setOnClickListener { onEdit(card) }
            btnDelete.setOnClickListener { onDelete(card) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<UserWord>() {
            override fun areItemsTheSame(a: UserWord, b: UserWord) = a.id == b.id
            override fun areContentsTheSame(a: UserWord, b: UserWord) = a == b
        }
    }
}
