package com.verso.dict.util

import com.verso.dict.data.DictionaryDbHelper
import com.verso.dict.data.Word

/**
 * Routes a single search query to the right lookup. Input containing CJK characters triggers
 * the reverse lookup (Chinese definition -> matching English words); otherwise the input is
 * treated as an English word and a forward lookup runs.
 */
object SearchEngine {

    private val cjk = Regex("[\\u4e00-\\u9fff]")

    fun isReverseQuery(input: String): Boolean = cjk.containsMatchIn(input)

    fun search(helper: DictionaryDbHelper, input: String): List<Word> =
        if (isReverseQuery(input)) helper.searchByDefinition(input)
        else helper.searchByWord(input)
}
