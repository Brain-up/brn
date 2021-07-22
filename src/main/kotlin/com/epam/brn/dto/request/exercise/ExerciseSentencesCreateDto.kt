package com.epam.brn.dto.request.exercise

import com.epam.brn.enums.Locale
import com.epam.brn.upload.csv.seriesMatrix.SeriesMatrixRecord
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ApiModel(value = "ExerciseSentencesCreateDto", description = "Request dto for create exercise 'sentences'")
data class ExerciseSentencesCreateDto(
    @ApiModelProperty(value = "Locale", required = true, example = "RU")
    @field:NotNull
    val locale: Locale,
    @ApiModelProperty(value = "Subgroup code", required = true, example = "sentence_with_6_words")
    @field:NotBlank
    val subGroup: String,
    @field:NotNull
    @ApiModelProperty(value = "Level", required = true, example = "1")
    val level: Int,
    @field:NotBlank
    @ApiModelProperty(value = "Exercise name", required = true, example = "Пойми предложение из 6 слов из 18")
    val exerciseName: String,
    @field:NotNull
    @ApiModelProperty(value = "Order number", required = true, example = "1")
    val orderNumber: Int,
    @ApiModelProperty(value = "Sets of words for creating sentences", required = true)
    @field:NotNull
    val words: SetOfWords
) {
    fun toSeriesMatrixRecord() = SeriesMatrixRecord(
        level = level,
        code = subGroup,
        exerciseName = exerciseName,
        orderNumber = orderNumber,
        words = words.toRecordList()
    )
}
