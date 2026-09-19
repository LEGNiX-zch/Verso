package com.verso.dict.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Separate writable database for user-created card sets and word cards. It is intentionally a
 * different file (verso_user.db) from the read-only built-in dictionary (verso_dict.db), so the
 * built-in vocabulary can never be modified or deleted by the user. Created fresh on first
 * access; no asset is copied. A default card set is auto-created on first open so the user can
 * start adding cards immediately without having to create a set first.
 */
class UserCardDbHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private val appContext = context.applicationContext

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Enable foreign-key cascade when a card set is deleted.
        db.execSQL("PRAGMA foreign_keys=ON")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${Table.CARD_SETS} (
                ${Table.COL_SET_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${Table.COL_SET_NAME} TEXT NOT NULL,
                ${Table.COL_SET_CREATED} INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${Table.USER_CARDS} (
                ${Table.COL_CARD_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${Table.COL_CARD_WORD} TEXT NOT NULL,
                ${Table.COL_CARD_DEF} TEXT NOT NULL,
                ${Table.COL_CARD_SET_ID} INTEGER NOT NULL,
                ${Table.COL_CARD_CREATED} INTEGER NOT NULL,
                FOREIGN KEY(${Table.COL_CARD_SET_ID})
                    REFERENCES ${Table.CARD_SETS}(${Table.COL_SET_ID})
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_user_card_word ON ${Table.USER_CARDS}(${Table.COL_CARD_WORD})")
        db.execSQL("CREATE INDEX idx_user_card_def ON ${Table.USER_CARDS}(${Table.COL_CARD_DEF})")
        db.execSQL("CREATE INDEX idx_user_card_set ON ${Table.USER_CARDS}(${Table.COL_CARD_SET_ID})")
        // Seed a default card set so the user can add cards right away.
        ensureDefaultSet(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${Table.USER_CARDS}")
        db.execSQL("DROP TABLE IF EXISTS ${Table.CARD_SETS}")
        onCreate(db)
    }

    private fun ensureDefaultSet(db: SQLiteDatabase) {
        val c = db.query(Table.CARD_SETS, null, null, null, null, null, null)
        val empty = c.use { it.count == 0 }
        if (empty) {
            val cv = ContentValues()
            cv.put(Table.COL_SET_NAME, DEFAULT_SET_NAME)
            cv.put(Table.COL_SET_CREATED, System.currentTimeMillis())
            db.insert(Table.CARD_SETS, null, cv)
        }
    }

    /** Opens (creating if needed) the user db and guarantees a default set exists. */
    fun open() {
        val db = writableDatabase
        ensureDefaultSet(db)
    }

    // ---- Card sets ----

    fun getCardSets(): List<CardSet> {
        val list = ArrayList<CardSet>()
        val db = readableDatabase
        val c = db.query(
            Table.CARD_SETS, null, null, null, null, null,
            "${Table.COL_SET_CREATED} ASC, ${Table.COL_SET_NAME} ASC"
        )
        c.use {
            while (it.moveToNext()) {
                list.add(
                    CardSet(
                        it.getLong(it.getColumnIndexOrThrow(Table.COL_SET_ID)),
                        it.getString(it.getColumnIndexOrThrow(Table.COL_SET_NAME)),
                        it.getLong(it.getColumnIndexOrThrow(Table.COL_SET_CREATED))
                    )
                )
            }
        }
        return list
    }

    fun addCardSet(name: String): Long {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(Table.COL_SET_NAME, name.trim().ifEmpty { DEFAULT_SET_NAME })
        cv.put(Table.COL_SET_CREATED, System.currentTimeMillis())
        return db.insert(Table.CARD_SETS, null, cv)
    }

    /** Delete a card set and (via cascade) all cards that belong to it. */
    fun deleteCardSet(id: Long) {
        val db = writableDatabase
        // Cascade handles cards; delete the set in a transaction for safety.
        db.beginTransaction()
        try {
            db.delete(Table.USER_CARDS, "${Table.COL_CARD_SET_ID}=?", arrayOf(id.toString()))
            db.delete(Table.CARD_SETS, "${Table.COL_SET_ID}=?", arrayOf(id.toString()))
            // Never let the last set disappear: re-create default if empty.
            val c = db.query(Table.CARD_SETS, null, null, null, null, null, null)
            if (c.use { it.count } == 0) {
                ensureDefaultSet(db)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // ---- User cards ----

    fun addUserCard(word: String, definition: String, setId: Long): Long {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(Table.COL_CARD_WORD, word.trim())
        cv.put(Table.COL_CARD_DEF, definition.trim())
        cv.put(Table.COL_CARD_SET_ID, setId)
        cv.put(Table.COL_CARD_CREATED, System.currentTimeMillis())
        return db.insert(Table.USER_CARDS, null, cv)
    }

    fun updateUserCard(id: Long, word: String, definition: String, setId: Long): Int {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(Table.COL_CARD_WORD, word.trim())
        cv.put(Table.COL_CARD_DEF, definition.trim())
        cv.put(Table.COL_CARD_SET_ID, setId)
        return db.update(
            Table.USER_CARDS, cv,
            "${Table.COL_CARD_ID}=?", arrayOf(id.toString())
        )
    }

    fun deleteUserCard(id: Long): Int {
        val db = writableDatabase
        return db.delete(
            Table.USER_CARDS, "${Table.COL_CARD_ID}=?", arrayOf(id.toString())
        )
    }

    /** All user cards, optionally filtered to a single card set. */
    fun getUserCards(setId: Long? = null): List<UserWord> {
        val list = ArrayList<UserWord>()
        val db = readableDatabase
        val c = if (setId != null) {
            db.query(
                Table.USER_CARDS, null,
                "${Table.COL_CARD_SET_ID}=?", arrayOf(setId.toString()),
                null, null, "${Table.COL_CARD_WORD} ASC"
            )
        } else {
            db.query(
                Table.USER_CARDS, null, null, null, null, null,
                "${Table.COL_CARD_WORD} ASC"
            )
        }
        c.use {
            while (it.moveToNext()) {
                list.add(readUserWord(it))
            }
        }
        return list
    }

    /** Forward search over user cards only. */
    fun searchByWord(keyword: String): List<UserWord> {
        val list = ArrayList<UserWord>()
        if (keyword.isBlank()) return list
        val key = "${keyword.trim().lowercase()}%"
        val db = readableDatabase
        val c = db.query(
            Table.USER_CARDS, null,
            "${Table.COL_CARD_WORD} LIKE ? COLLATE NOCASE",
            arrayOf(key), null, null, "${Table.COL_CARD_WORD} ASC"
        )
        c.use {
            while (it.moveToNext()) {
                list.add(readUserWord(it))
            }
        }
        return list
    }

    /** Reverse search over user cards only. */
    fun searchByDefinition(keyword: String): List<UserWord> {
        val list = ArrayList<UserWord>()
        if (keyword.isBlank()) return list
        val key = "%${keyword.trim()}%"
        val db = readableDatabase
        val c = db.query(
            Table.USER_CARDS, null,
            "${Table.COL_CARD_DEF} LIKE ?",
            arrayOf(key), null, null, "${Table.COL_CARD_WORD} ASC"
        )
        c.use {
            while (it.moveToNext()) {
                list.add(readUserWord(it))
            }
        }
        return list
    }

    private fun readUserWord(c: android.database.Cursor): UserWord =
        UserWord(
            c.getLong(c.getColumnIndexOrThrow(Table.COL_CARD_ID)),
            c.getString(c.getColumnIndexOrThrow(Table.COL_CARD_WORD)),
            c.getString(c.getColumnIndexOrThrow(Table.COL_CARD_DEF)),
            c.getLong(c.getColumnIndexOrThrow(Table.COL_CARD_SET_ID))
        )

    object Table {
        const val CARD_SETS = "card_sets"
        const val USER_CARDS = "user_cards"
        const val COL_SET_ID = "_id"
        const val COL_SET_NAME = "name"
        const val COL_SET_CREATED = "created_at"
        const val COL_CARD_ID = "_id"
        const val COL_CARD_WORD = "word"
        const val COL_CARD_DEF = "definition"
        const val COL_CARD_SET_ID = "set_id"
        const val COL_CARD_CREATED = "created_at"
    }

    companion object {
        private const val DB_NAME = "verso_user.db"
        private const val DB_VERSION = 1
        const val DEFAULT_SET_NAME = "默认词卡集"
    }
}
