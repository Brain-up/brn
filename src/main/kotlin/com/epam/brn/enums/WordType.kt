package com.epam.brn.enums

enum class WordType {
    OBJECT,
    OBJECT_ACTION,
    OBJECT_DESCRIPTION,
    ADDITION_OBJECT,
    ADDITION_OBJECT_DESCRIPTION,
    COUNT,
    PHRASE,
    SENTENCE,
    AUDIOMETRY_WORD,
    UNKNOWN;

    companion object {
        fun of(wordGroupPosition: Int): WordType {
            return when (wordGroupPosition) {
                0 -> COUNT
                1 -> OBJECT_DESCRIPTION
                2 -> OBJECT
                3 -> OBJECT_ACTION
                4 -> ADDITION_OBJECT_DESCRIPTION
                5 -> ADDITION_OBJECT
                else -> UNKNOWN
            }
        }
    }
}
