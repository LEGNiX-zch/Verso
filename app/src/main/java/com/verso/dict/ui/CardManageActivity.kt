package com.verso.dict.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.verso.dict.R
import com.verso.dict.data.CardSet
import com.verso.dict.data.UserCardDbHelper
import com.verso.dict.data.UserWord
import com.verso.dict.databinding.ActivityCardManageBinding

/**
 * View / edit / delete the user's own custom cards. The built-in dictionary is completely
 * invisible here. A spinner at the top filters the list by card set (plus an "all" option).
 */
class CardManageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardManageBinding
    private lateinit var userDb: UserCardDbHelper
    private lateinit var adapter: UserCardAdapter

    private var sets: List<CardSet> = emptyList()
    private var filterId: Long = ALL_SETS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        userDb = UserCardDbHelper(this)
        userDb.open()

        adapter = UserCardAdapter(onEdit = ::showEditDialog, onDelete = ::confirmDelete)
        binding.rvCards.layoutManager = LinearLayoutManager(this)
        binding.rvCards.adapter = adapter

        loadFilter()
        refresh()
    }

    override fun onResume() {
        super.onResume()
        // The set list may have changed in Settings; refresh the filter and the list.
        loadFilter()
        refresh()
    }

    private fun loadFilter() {
        sets = userDb.getCardSets()
        val labels = ArrayList<String>()
        labels.add(getString(R.string.card_manage_filter_all))
        labels.addAll(sets.map { it.name })
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val previousSelection = binding.spFilter.selectedItemPosition
        binding.spFilter.adapter = spinnerAdapter
        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                filterId = if (pos == 0) ALL_SETS else sets.getOrNull(pos - 1)?.id ?: ALL_SETS
                refresh()
            }
            override fun onNothingSelected(p: AdapterView<*>?) { filterId = ALL_SETS }
        }
        if (previousSelection in 0 until labels.size) {
            binding.spFilter.setSelection(previousSelection)
        }
    }

    private fun refresh() {
        val cards = if (filterId == ALL_SETS) userDb.getUserCards() else userDb.getUserCards(filterId)
        adapter.submitList(cards)
        binding.tvEmpty.visibility = if (cards.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showEditDialog(card: UserWord) {
        val view = layoutInflater.inflate(R.layout.dialog_edit_card, null)
        val etWord = view.findViewById<android.widget.EditText>(R.id.et_edit_word)
        val etDef = view.findViewById<android.widget.EditText>(R.id.et_edit_definition)
        val spSet = view.findViewById<android.widget.Spinner>(R.id.sp_edit_set)

        etWord.setText(card.word)
        etDef.setText(card.definition)

        // Populate the set spinner for this edit dialog.
        sets = userDb.getCardSets()
        val names = sets.map { it.name }
        val setAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        setAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSet.adapter = setAdapter
        val currentSetIndex = sets.indexOfFirst { it.id == card.setId }
        if (currentSetIndex >= 0) spSet.setSelection(currentSetIndex)
        var chosenSetId = sets.getOrNull(currentSetIndex)?.id ?: -1L
        spSet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                chosenSetId = sets.getOrNull(pos)?.id ?: -1L
            }
            override fun onNothingSelected(p: AdapterView<*>?) { chosenSetId = -1L }
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.card_manage_edit)
            .setView(view)
            .setPositiveButton(R.string.card_manage_confirm) { _, _ ->
                val w = etWord.text.toString().trim()
                val d = etDef.text.toString().trim()
                if (w.isNotEmpty() && d.isNotEmpty() && chosenSetId > 0) {
                    userDb.updateUserCard(card.id, w, d, chosenSetId)
                    refresh()
                }
            }
            .setNegativeButton(R.string.card_manage_cancel, null)
            .show()
    }

    private fun confirmDelete(card: UserWord) {
        AlertDialog.Builder(this)
            .setMessage(R.string.card_manage_confirm_delete)
            .setPositiveButton(R.string.card_manage_confirm) { _, _ ->
                userDb.deleteUserCard(card.id)
                refresh()
            }
            .setNegativeButton(R.string.card_manage_cancel, null)
            .show()
    }

    companion object {
        private const val ALL_SETS = -1L
    }
}
