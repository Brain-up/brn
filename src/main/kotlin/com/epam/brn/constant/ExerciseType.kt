package com.epam.brn.constant

enum class ExerciseType {
    SINGLE_WORDS,
    WORDS_SEQUENCES,
    SENTENCE;

    companion object {
        fun of(seriesId: Long): ExerciseType {
            return when (seriesId) {
                1L -> SINGLE_WORDS
                2L -> WORDS_SEQUENCES
                3L -> SENTENCE
                else -> throw IllegalArgumentException("There no ExerciseType for seriesId=$seriesId")
            }
        }
    }
}
