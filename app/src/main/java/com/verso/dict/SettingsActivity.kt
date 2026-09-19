package com.verso.dict

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.verso.dict.databinding.ActivitySettingsBinding
import com.verso.dict.util.FontSizeManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

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
}
