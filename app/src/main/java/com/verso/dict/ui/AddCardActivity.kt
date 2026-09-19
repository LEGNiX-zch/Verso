package com.verso.dict.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.verso.dict.R
import com.verso.dict.data.CardSet
import com.verso.dict.data.UserCardDbHelper
import com.verso.dict.databinding.ActivityAddCardBinding
import com.verso.dict.util.FontSizeManager

/**
 * Lets the user manually add a custom word card (English word + Chinese definition) and choose
 * which of their card sets it belongs to. The built-in dictionary is never touched. A default
 * set is guaranteed to exist so the spinner is never empty. The set selector uses
 * [GlassSpinnerAdapter] so the currently-active set shows a blue selection ring.
 */
class AddCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCardBinding
    private lateinit var userDb: UserCardDbHelper
    private var sets: List<CardSet> = emptyList()
    private var selectedSetId: Long = -1L
    private var spinnerAdapter: GlassSpinnerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        userDb = UserCardDbHelper(this)
        userDb.open()
        loadSets()
    }

    private fun loadSets() {
        sets = userDb.getCardSets()
        if (sets.isEmpty()) {
            // Should not happen (default set auto-created), but guard anyway.
            userDb.open()
            sets = userDb.getCardSets()
        }
        val names = sets.map { it.name }
        val adapter = GlassSpinnerAdapter(this, names)
        binding.spSet.adapter = adapter
        spinnerAdapter = adapter
        binding.spSet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                selectedSetId = sets.getOrNull(pos)?.id ?: -1L
                spinnerAdapter?.selectedPosition = pos
            }
            override fun onNothingSelected(p: AdapterView<*>?) { selectedSetId = -1L }
        }
        if (sets.isNotEmpty()) {
            selectedSetId = sets[0].id
            spinnerAdapter?.selectedPosition = 0
        }

        binding.btnSave.setOnClickListener {
            val word = binding.etWord.text.toString().trim()
            val def = binding.etDefinition.text.toString().trim()
            if (word.isEmpty() || def.isEmpty() || selectedSetId < 0) {
                Toast.makeText(this, R.string.add_card_invalid, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            userDb.addUserCard(word, def, selectedSetId)
            Toast.makeText(this, R.string.add_card_saved, Toast.LENGTH_SHORT).show()
            binding.etWord.setText("")
            binding.etDefinition.setText("")
            binding.etWord.requestFocus()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the set list in case the user created/deleted a set in Settings meanwhile.
        val refreshed = userDb.getCardSets()
        if (refreshed != sets) {
            sets = refreshed
            val names = sets.map { it.name }
            val adapter = GlassSpinnerAdapter(this, names)
            binding.spSet.adapter = adapter
            spinnerAdapter = adapter
            if (sets.isNotEmpty()) spinnerAdapter?.selectedPosition = 0
        }
    }
}
