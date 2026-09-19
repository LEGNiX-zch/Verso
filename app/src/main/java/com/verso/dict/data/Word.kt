package com.verso.dict.data

/** A single dictionary entry: an English word and its Chinese definition. */
data class Word(
    val id: Long,
    val word: String,
    val definition: String
)
