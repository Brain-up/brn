package com.epam.brn.dto.request.exercise

import com.epam.brn.enums.Locale
import com.epam.brn.upload.csv.seriesPhrases.SeriesPhrasesRecord
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ApiModel(value = "ExercisePhrasesCreateDto", description = "Request dto for create exercise 'phrases'")
data class ExercisePhrasesCreateDto(
    @ApiModelProperty(value = "Locale", required = true, example = "RU")
    @field:NotNull
    val locale: Locale,
    @ApiModelProperty(value = "Subgroup code", required = true, example = "longShortPhrases")
    @field:NotBlank
    val subGroup: String,
    @ApiModelProperty(value = "Level", required = true, example = "1")
    @field:NotNull
    val level: Int,
    @ApiModelProperty(value = "Exercise name", required = true, example = "Фразы разной длительности")
    @field:NotBlank
    val exerciseName: String,
    @ApiModelProperty(value = "Phrases for creating exercise. Phrases should not contain punctuation marks.", required = true)
    @field:Valid
    val phrases: Phrases,
    @ApiModelProperty(value = "noise level", required = true, example = "50")
    @field:NotNull
    val noiseLevel: Int,
    @ApiModelProperty(value = "noise url", required = false, example = "voices")
    val noiseUrl: String? = null
) {
    fun toSeriesPhrasesRecord() = SeriesPhrasesRecord(
        level = level,
        code = subGroup,
        exerciseName = exerciseName,
        phrases = phrases.toListWithDot(),
        noiseLevel = noiseLevel,
        noiseUrl = noiseUrl.orEmpty()
    )
}
