package com.verso.dict

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.verso.dict.data.DictionaryDbHelper
import com.verso.dict.data.UserCardDbHelper
import com.verso.dict.databinding.ActivityMainBinding
import com.verso.dict.ui.AddCardActivity
import com.verso.dict.ui.CardManageActivity
import com.verso.dict.ui.WordAdapter
import com.verso.dict.ui.WordDialogFragment
import com.verso.dict.util.CombinedSearchEngine
import com.verso.dict.util.FontSizeManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DictionaryDbHelper
    private lateinit var userDb: UserCardDbHelper
    private lateinit var adapter: WordAdapter
    @Volatile private var dbReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the launch screen (animated book icon on brand background). Must run before
        // super.onCreate() so the backport swaps the starting theme for Theme.Verso correctly.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DictionaryDbHelper(this)
        userDb = UserCardDbHelper(this)
        userDb.open()

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
                R.id.nav_add_card -> AddCardActivity::class.java
                R.id.nav_card_manage -> CardManageActivity::class.java
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
        // Staggered glass entrance for search results.
        binding.resultsList.layoutAnimation =
            android.view.animation.AnimationUtils.loadLayoutAnimation(this, R.anim.glass_layout_anim)

        binding.searchButton.setOnClickListener { doSearch() }

        // One-time copy of the prebuilt dictionary database (shipped in assets) into the app's
        // private database directory. Run off the main thread so the OPPO Watch UI is not blocked.
        if (!dbReady) {
            Thread {
                db.ensureDatabase()
                dbReady = true
            }.start()
        }

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
        // If the one-time database copy from assets is still running, wait briefly for it.
        Thread {
            val maxWait = 8000L
            val step = 50L
            var waited = 0L
            while (!dbReady && waited < maxWait) {
                Thread.sleep(step); waited += step
            }
            val results = CombinedSearchEngine.search(this, db, userDb, query)
            runOnUiThread {
                if (results.isEmpty()) {
                    showEmpty(getString(R.string.empty_results))
                } else {
                    binding.emptyView.visibility = View.GONE
                    adapter.submitList(results)
                    binding.resultsList.scheduleLayoutAnimation()
                }
                hideKeyboard()
            }
        }.start()
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
