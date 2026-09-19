package com.verso.dict.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.verso.dict.R
import com.verso.dict.data.Word
import com.verso.dict.util.FontSizeManager

/** Adapter for the search results list. Font sizes follow the global font setting. */
class WordAdapter(
    private val onClick: (Word) -> Unit
) : ListAdapter<Word, WordAdapter.WordVH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordVH(view)
    }

    override fun onBindViewHolder(holder: WordVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WordVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val wordText: TextView = itemView.findViewById(R.id.tv_word)
        private val defText: TextView = itemView.findViewById(R.id.tv_definition)

        fun bind(word: Word) {
            wordText.text = word.word
            defText.text = word.definition
            FontSizeManager.applyScaled(itemView.context, wordText, R.dimen.word_text)
            FontSizeManager.applyScaled(itemView.context, defText, R.dimen.definition_text)
            itemView.setOnClickListener { onClick(word) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Word>() {
            override fun areItemsTheSame(a: Word, b: Word) = a.id == b.id
            override fun areContentsTheSame(a: Word, b: Word) = a == b
        }
    }
}
