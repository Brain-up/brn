package com.epam.brn.dto.request.exercise

import com.epam.brn.enums.Locale
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class ExerciseSentencesCreateDto(
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
    @NotNull
    val orderNumber: Int,
    @NotEmpty
    val words: List<String>
)
