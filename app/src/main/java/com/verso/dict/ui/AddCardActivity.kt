package com.verso.dict.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.verso.dict.R
import com.verso.dict.data.CardSet
import com.verso.dict.data.UserCardDbHelper
import com.verso.dict.databinding.ActivityAddCardBinding

/**
 * Lets the user manually add a custom word card (English word + Chinese definition) and choose
 * which of their card sets it belongs to. The built-in dictionary is never touched. A default
 * set is guaranteed to exist so the spinner is never empty.
 */
class AddCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCardBinding
    private lateinit var userDb: UserCardDbHelper
    private var sets: List<CardSet> = emptyList()
    private var selectedSetId: Long = -1L

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
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spSet.adapter = adapter
        binding.spSet.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                selectedSetId = sets.getOrNull(pos)?.id ?: -1L
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) { selectedSetId = -1L }
        }
        if (sets.isNotEmpty()) {
            selectedSetId = sets[0].id
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
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spSet.adapter = adapter
        }
    }
}
