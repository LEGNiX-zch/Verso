package com.verso.dict.data

/** A custom word card created by the user. Belongs to a [CardSet]; never touches the read-only
 *  built-in dictionary. */
data class UserWord(
    val id: Long,
    val word: String,
    val definition: String,
    val setId: Long
)
