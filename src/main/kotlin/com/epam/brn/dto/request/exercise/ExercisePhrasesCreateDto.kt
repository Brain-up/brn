package com.epam.brn.dto.request.exercise

import com.epam.brn.enums.Locale
import com.epam.brn.upload.csv.seriesPhrases.SeriesPhrasesRecord
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@ApiModel(value = "ExercisePhrasesCreateDto", description = "request dto for create exercise 'phrases'")
data class ExercisePhrasesCreateDto(
    @ApiModelProperty(value = "locale", required = true, example = "RU")
    @field:NotNull
    val locale: Locale,
    @ApiModelProperty(value = "subgroup code", required = true, example = "longShortPhrases")
    @field:NotBlank
    val subGroup: String,
    @ApiModelProperty(value = "level", required = true, example = "1")
    @field:NotNull
    val level: Int,
    @ApiModelProperty(value = "exercise name", required = true, example = "Фразы разной длительности")
    @field:NotBlank
    val exerciseName: String,
    @ApiModelProperty(value = "phrases for creating exercise", required = true, example = "[Мамочка идёт, Мамочка быстро идёт в магазин]")
    @field:NotEmpty
    val phrases: List<String>,
    @ApiModelProperty(value = "noise level", required = true, example = "50")
    @field:NotNull
    val noiseLevel: Int,
    @ApiModelProperty(value = "noise url", required = false, example = "voices")
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
