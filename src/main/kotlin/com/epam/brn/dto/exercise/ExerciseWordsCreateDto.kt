package com.epam.brn.dto.exercise

import com.epam.brn.enums.Locale
import com.epam.brn.upload.csv.seriesWords.SeriesWordsRecord
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class ExerciseWordsCreateDto(
    @NotNull
    val locale: Locale,
    @NotBlank
    val subGroup: String,
    @NotNull
    val level: Int,
    @NotBlank
    val exerciseName: String,
    @NotEmpty
    val words: List<String>,
    @NotNull
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
