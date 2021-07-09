package com.epam.brn.dto.exercise

import com.epam.brn.enums.Locale
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class ExercisePhrasesCreateDto(
    @NotNull
    val locale: Locale,
    @NotNull
    val subGroup: Int,
    @NotNull
    val level: Int,
    @NotBlank
    val code: String,
    @NotBlank
    val exerciseName: String,
    @NotBlank
    val phrases: String,
    @NotNull
    val noiseLevel: Int,
    val noiseUrl: String?
)
