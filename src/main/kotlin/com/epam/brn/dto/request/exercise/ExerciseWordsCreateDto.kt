package com.epam.brn.dto.request.exercise

import com.epam.brn.enums.Locale
import com.epam.brn.upload.csv.seriesWords.SeriesWordsRecord
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class ExerciseWordsCreateDto(
    @field:NotNull
    val locale: Locale,
    @field:NotBlank
    val subGroup: String,
    @field:NotNull
    val level: Int,
    @field:NotBlank
    val exerciseName: String,
    @field:NotEmpty
    val words: List<String>,
    @field:NotNull
    val noiseLevel: Int,
    val noiseUrl: String?
) {
    fun toSeriesWordsRecord() = SeriesWordsRecord(
        level = level,
        code = subGroup,
        exerciseName = exerciseName,
        words = words,
        noiseLevel = noiseLevel,
        noiseUrl = noiseUrl.orEmpty()
    )
}
