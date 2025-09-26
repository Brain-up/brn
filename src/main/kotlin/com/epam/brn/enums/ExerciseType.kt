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
    SYLLABLES_KOROLEVA,
}

val exercisesWithPictures =
    setOf(
        ExerciseType.WORDS_SEQUENCES,
        ExerciseType.SENTENCE,
        ExerciseType.SINGLE_SIMPLE_WORDS,
        ExerciseType.SINGLE_WORDS_KOROLEVA,
        ExerciseType.FREQUENCY_WORDS,
        ExerciseType.PHRASES,
    )

fun ExerciseType.shouldBeWithPictures(): Boolean = exercisesWithPictures.contains(this)

fun ExerciseType.toMechanism(): ExerciseMechanism =
    mapExerciseTypeToMechanism
        .getOrElse(this) { throw IllegalArgumentException("No ExerciseMechanism is defined for $this.") }

val mapExerciseTypeToMechanism =
    mapOf(
        ExerciseType.SINGLE_SIMPLE_WORDS to ExerciseMechanism.WORDS,
        ExerciseType.SINGLE_WORDS_KOROLEVA to ExerciseMechanism.WORDS,
        ExerciseType.PHRASES to ExerciseMechanism.WORDS,
        ExerciseType.FREQUENCY_WORDS to ExerciseMechanism.WORDS,
        ExerciseType.SYLLABLES_KOROLEVA to ExerciseMechanism.WORDS,
        ExerciseType.SENTENCE to ExerciseMechanism.MATRIX,
        ExerciseType.WORDS_SEQUENCES to ExerciseMechanism.MATRIX,
        ExerciseType.DURATION_SIGNALS to ExerciseMechanism.SIGNALS,
        ExerciseType.FREQUENCY_SIGNALS to ExerciseMechanism.SIGNALS,
    )
