package com.verso.dict.util

import android.content.Context
import com.verso.dict.data.DictionaryDbHelper
import com.verso.dict.data.UserCardDbHelper
import com.verso.dict.data.UserWord
import com.verso.dict.data.Word

/**
 * Combines built-in dictionary search with user-created card search. The built-in vocabulary is
 * only queried when the "hide built-in" preference is OFF; user cards are always queried. User
 * cards are converted to [Word] for display so the existing list adapter and detail dialog stay
 * unchanged. Reuses [SearchEngine.isReverseQuery] so forward/reverse routing is identical.
 */
object CombinedSearchEngine {

    // Offset user-card ids when presenting them as Word so DiffUtil never confuses a user card
    // with a built-in entry that happens to share the same numeric id.
    private const val USER_ID_OFFSET = 1_000_000_000L

    fun isHideBuiltin(context: Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences("verso_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_HIDE_BUILTIN, false)
    }

    fun setHideBuiltin(context: Context, hide: Boolean) {
        val prefs = context.applicationContext.getSharedPreferences("verso_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HIDE_BUILTIN, hide).apply()
    }

    fun search(
        context: Context,
        builtin: DictionaryDbHelper,
        user: UserCardDbHelper,
        input: String
    ): List<Word> {
        val reverse = SearchEngine.isReverseQuery(input)
        val results = ArrayList<Word>()

        // Built-in vocabulary is read-only here; only queried when not hidden.
        if (!isHideBuiltin(context)) {
            results += if (reverse) builtin.searchByDefinition(input) else builtin.searchByWord(input)
        }

        // User cards are always searched regardless of the hide-builtin toggle.
        val userResults: List<UserWord> =
            if (reverse) user.searchByDefinition(input) else user.searchByWord(input)
        results += userResults.map { it.toWord() }

        // Deduplicate by lowercased word (a user card and a built-in entry may share a word).
        val seen = HashSet<String>()
        return results.filter { seen.add(it.word.lowercase()) }
    }

    private fun UserWord.toWord(): Word = Word(id + USER_ID_OFFSET, word, definition)

    private const val KEY_HIDE_BUILTIN = "hide_builtin"
}
