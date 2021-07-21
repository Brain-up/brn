package com.epam.brn.dto.request.exercise

import com.epam.brn.enums.Locale
import com.epam.brn.upload.csv.seriesWords.SeriesWordsRecord
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@ApiModel(value = "ExerciseWordsCreateDto", description = "request dto for create exercise 'words'")
data class ExerciseWordsCreateDto(
    @ApiModelProperty(value = "locale", required = true, example = "RU")
    @field:NotNull
    val locale: Locale,
    @ApiModelProperty(value = "subgroup code", required = true, example = "family")
    @field:NotBlank
    val subGroup: String,
    @ApiModelProperty(value = "level", required = true, example = "1")
    @field:NotNull
    val level: Int,
    @ApiModelProperty(value = "exercise name", required = true, example = "Семья /+голоса/")
    @field:NotBlank
    val exerciseName: String,
    @ApiModelProperty(value = "words for creating exercise", required = true, example = "[сын, ребёнок, мама]")
    @field:NotEmpty
    val words: List<String>,
    @ApiModelProperty(value = "noise level", required = true, example = "50")
    @field:NotNull
    val noiseLevel: Int,
    @ApiModelProperty(value = "noise url", required = false, example = "voices")
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
