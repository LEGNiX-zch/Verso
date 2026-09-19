package com.verso.dict

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.verso.dict.data.DictionaryDbHelper
import com.verso.dict.databinding.ActivityMainBinding
import com.verso.dict.ui.WordAdapter
import com.verso.dict.ui.WordDialogFragment
import com.verso.dict.util.FontSizeManager
import com.verso.dict.util.SearchEngine

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DictionaryDbHelper
    private lateinit var adapter: WordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DictionaryDbHelper(this)

        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener { item ->
            val target = when (item.itemId) {
                R.id.nav_settings -> SettingsActivity::class.java
                R.id.nav_about -> AboutActivity::class.java
                else -> null
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            target?.let { startActivity(android.content.Intent(this, it)) }
            true
        }

        adapter = WordAdapter { word ->
            WordDialogFragment.newInstance(word)
                .show(supportFragmentManager, "word_dialog")
        }
        binding.resultsList.layoutManager = LinearLayoutManager(this)
        binding.resultsList.adapter = adapter

        binding.searchButton.setOnClickListener { doSearch() }

        // Refresh list fonts when returning from settings (font level may have changed).
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        // Re-bind so font changes made in settings take effect immediately on return.
        adapter.notifyDataSetChanged()
    }

    private fun doSearch() {
        val query = binding.searchEdit.text.toString().trim()
        if (query.isEmpty()) {
            showEmpty(getString(R.string.empty_results))
            return
        }
        val results = SearchEngine.search(db, query)
        if (results.isEmpty()) {
            showEmpty(getString(R.string.empty_results))
        } else {
            binding.emptyView.visibility = View.GONE
            adapter.submitList(results)
        }
        hideKeyboard()
    }

    private fun showEmpty(message: String) {
        adapter.submitList(emptyList())
        binding.emptyView.text = message
        binding.emptyView.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEdit.windowToken, 0)
    }

    @Deprecated("Back press closes the drawer first if open")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
