package com.epam.brn.dto.request.exercise

import com.epam.brn.enums.Locale
import com.epam.brn.upload.csv.seriesPhrases.SeriesPhrasesRecord
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class ExercisePhrasesCreateDto(
    @field:NotNull
    val locale: Locale,
    @field:NotBlank
    val subGroup: String,
    @field:NotNull
    val level: Int,
    @field:NotBlank
    val exerciseName: String,
    @field:NotEmpty
    val phrases: List<String>,
    @field:NotNull
    val noiseLevel: Int,
    val noiseUrl: String?
) {
    fun toSeriesPhrasesRecord() = SeriesPhrasesRecord(
        level = level,
        code = subGroup,
        exerciseName = exerciseName,
        phrases = phrases,
        noiseLevel = noiseLevel,
        noiseUrl = noiseUrl.orEmpty()
    )
}
