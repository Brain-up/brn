package com.epam.brn.model

enum class ExerciseType(val audioFileSeriesIndex: Int) {
    WORDS_SEQUENCES(2),
    SENTENCE(3),
    SINGLE_SIMPLE_WORDS(1),
    PHRASES(4),
    FREQUENCY_WORDS(1),
    DI(-1),
    DURATION_SIGNALS(-1),
    FREQUENCY_SIGNALS(-1);
}
