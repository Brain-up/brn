package com.epam.brn.constant

enum class ExerciseTypeEnum {
    SINGLE_WORDS,
    WORDS_SEQUENCES,
    SENTENCE;

    companion object {
        fun of(seriesId: Long): ExerciseTypeEnum {
            return when (seriesId) {
                1L -> SINGLE_WORDS
                2L -> WORDS_SEQUENCES
                3L -> SENTENCE
                else -> throw IllegalArgumentException("There no ExerciseType for seriesId=$seriesId")
            }
        }
    }
}
