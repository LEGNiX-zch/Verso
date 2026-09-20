package com.verso.dict

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.verso.dict.data.UserCardDbHelper
import com.verso.dict.databinding.ActivitySettingsBinding
import com.verso.dict.ui.CardSetAdapter
import com.verso.dict.util.CombinedSearchEngine
import com.verso.dict.util.FontSizeManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var userDb: UserCardDbHelper
    private lateinit var setAdapter: CardSetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        userDb = UserCardDbHelper(this)
        userDb.open()

        setupFontSection()
        setupCardSetSection()
        setupHideBuiltinSection()
    }

    override fun onResume() {
        super.onResume()
        // Card sets may have been changed indirectly (e.g. default set auto-created
        // by AddCardActivity), so refresh on every return.
        loadCardSets()
    }

    // ---------- Existing font-size section (unchanged) ----------

    private fun setupFontSection() {
        val currentLevel = FontSizeManager.getLevel(this)
        binding.fontSizeSeekbar.max = 6
        binding.fontSizeSeekbar.progress = currentLevel
        updatePreview(currentLevel)

        binding.fontSizeSeekbar.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                updatePreview(progress)
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                FontSizeManager.setLevel(this@SettingsActivity, binding.fontSizeSeekbar.progress)
            }
        })
    }

    private fun updatePreview(level: Int) {
        val prefs = getSharedPreferences("verso_prefs", MODE_PRIVATE)
        prefs.edit().putInt("font_level", level.coerceIn(0, 6)).apply()
        FontSizeManager.applyScaled(this, binding.tvFontPreview, R.dimen.word_text)
    }

    // ---------- New: card-set CRUD section ----------

    private fun setupCardSetSection() {
        setAdapter = CardSetAdapter { set -> confirmDeleteSet(set) }
        binding.rvCardSets.layoutManager = LinearLayoutManager(this)
        binding.rvCardSets.setHasFixedSize(false)
        binding.rvCardSets.adapter = setAdapter

        binding.btnCreateSet.setOnClickListener {
            val name = binding.etNewSet.text?.toString().orEmpty().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.settings_set_name_hint, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            userDb.addCardSet(name)
            binding.etNewSet.text?.clear()
            loadCardSets()
        }

        loadCardSets()
    }

    private fun loadCardSets() {
        val sets = userDb.getCardSets()
        setAdapter.submitList(sets)
        binding.tvNoSets.visibility = if (sets.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun confirmDeleteSet(set: com.verso.dict.data.CardSet) {
        AlertDialog.Builder(this)
            .setMessage(R.string.settings_confirm_delete_set)
            .setPositiveButton(R.string.card_manage_confirm) { _, _ ->
                userDb.deleteCardSet(set.id)
                loadCardSets()
            }
            .setNegativeButton(R.string.card_manage_cancel, null)
            .show()
    }

    // ---------- New: hide built-in dictionary toggle ----------

    private fun setupHideBuiltinSection() {
        binding.swHideBuiltin.isChecked = CombinedSearchEngine.isHideBuiltin(this)
        binding.swHideBuiltin.setOnCheckedChangeListener { _, checked ->
            CombinedSearchEngine.setHideBuiltin(this, checked)
        }
    }
}
