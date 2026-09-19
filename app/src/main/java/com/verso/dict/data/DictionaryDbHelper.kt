package com.verso.dict.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream

/**
 * Local offline dictionary database.
 *
 * A prebuilt SQLite database (~57k core English-Chinese entries, derived from the MIT-licensed
 * ECDICT corpus) is shipped in assets/verso_dict.db. On first launch it is copied into the app's
 * private database directory so lookups are instant and require no network access. If the asset
 * is ever missing (e.g. a build without the bundled db), a small fallback table is created so the
 * app still runs instead of crashing.
 */
class DictionaryDbHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private val appContext = context.applicationContext

    override fun onCreate(db: SQLiteDatabase) {
        // The real data comes from the prebuilt asset copied in ensureDatabase(); this is only a
        // safety-net schema for the unlikely case the asset cannot be copied.
        createEmptySchema(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        createEmptySchema(db)
    }

    /** Forward search: English word prefix match. */
    fun searchByWord(keyword: String): List<Word> {
        val results = ArrayList<Word>()
        if (keyword.isBlank()) return results
        val key = "${keyword.trim().lowercase()}%"
        val db = readableDatabase
        val c = db.query(
            Table.WORDS,
            null,
            "${Table.COL_WORD} LIKE ? COLLATE NOCASE",
            arrayOf(key),
            null,
            null,
            "${Table.COL_WORD} ASC"
        )
        c.use {
            while (it.moveToNext()) {
                results.add(readRow(it))
            }
        }
        return results
    }

    /** Reverse search: match English words whose Chinese definition contains the keyword. */
    fun searchByDefinition(keyword: String): List<Word> {
        val results = ArrayList<Word>()
        if (keyword.isBlank()) return results
        val key = "%${keyword.trim()}%"
        val db = readableDatabase
        val c = db.query(
            Table.WORDS,
            null,
            "${Table.COL_DEFINITION} LIKE ?",
            arrayOf(key),
            null,
            null,
            "${Table.COL_WORD} ASC"
        )
        c.use {
            while (it.moveToNext()) {
                results.add(readRow(it))
            }
        }
        return results
    }

    private fun readRow(c: android.database.Cursor): Word =
        Word(
            c.getLong(c.getColumnIndexOrThrow(Table.COL_ID)),
            c.getString(c.getColumnIndexOrThrow(Table.COL_WORD)),
            c.getString(c.getColumnIndexOrThrow(Table.COL_DEFINITION))
        )

    private fun createEmptySchema(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ${Table.WORDS} (
                ${Table.COL_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${Table.COL_WORD} TEXT NOT NULL,
                ${Table.COL_DEFINITION} TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_word ON ${Table.WORDS}(${Table.COL_WORD})")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_definition ON ${Table.WORDS}(${Table.COL_DEFINITION})")
    }

    /**
     * On first use, copy the prebuilt dictionary database from assets into the app's database
     * directory. After that, SQLiteOpenHelper opens the ready-made db directly. This is called
     * lazily so MainActivity can show results immediately once the one-time copy finishes.
     */
    fun ensureDatabase() {
        val dbFile = appContext.getDatabasePath(DB_NAME)
        if (dbFile.exists() && dbFile.length() > MIN_DB_BYTES) return
        dbFile.parentFile?.mkdirs()
        try {
            appContext.assets.open(DB_NAME).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            // Force SQLiteOpenHelper to recognise the freshly copied db.
            close()
        } catch (e: Exception) {
            // Asset missing/corrupt: fall back to an empty schema so the app still runs.
            val db = writableDatabase
            createEmptySchema(db)
            db.close()
        }
    }

    object Table {
        const val WORDS = "words"
        const val TABLE_WORDS = "words"
        const val COL_ID = "_id"
        const val COL_WORD = "word"
        const val COL_DEFINITION = "definition"
    }

    companion object {
        private const val DB_NAME = "verso_dict.db"
        private const val DB_VERSION = 1
        // Threshold below which we treat a "db exists" as actually empty/incomplete.
        private const val MIN_DB_BYTES = 1024L
    }
}
