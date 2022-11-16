package com.epam.brn.enums

enum class ExerciseType {
    WORDS_SEQUENCES,
    SENTENCE,
    SINGLE_SIMPLE_WORDS,
    SINGLE_WORDS_KOROLEVA,
    PHRASES,
    FREQUENCY_WORDS,
    DI,
    DURATION_SIGNALS,
    FREQUENCY_SIGNALS,
    SYLLABLES_KOROLEVA;
}

fun ExerciseType.toMechanism(): ExerciseMechanism =
    when (this) {
        ExerciseType.SINGLE_SIMPLE_WORDS, ExerciseType.SINGLE_WORDS_KOROLEVA, ExerciseType.PHRASES, ExerciseType.FREQUENCY_WORDS, ExerciseType.SYLLABLES_KOROLEVA -> ExerciseMechanism.WORDS
        ExerciseType.SENTENCE -> ExerciseMechanism.SENTENCES
        ExerciseType.WORDS_SEQUENCES -> ExerciseMechanism.MATRIX
        ExerciseType.DURATION_SIGNALS, ExerciseType.FREQUENCY_SIGNALS -> ExerciseMechanism.SIGNALS
        else -> throw IllegalArgumentException()
    }
