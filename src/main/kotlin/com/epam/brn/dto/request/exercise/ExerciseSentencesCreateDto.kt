package com.epam.brn.dto.request.exercise

import com.epam.brn.enums.Locale
import com.epam.brn.upload.csv.seriesMatrix.SeriesMatrixRecord
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.apache.commons.lang3.StringUtils
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ApiModel(value = "ExerciseSentencesCreateDto", description = "request dto for create exercise 'sentences'")
data class ExerciseSentencesCreateDto(
    @ApiModelProperty(value = "locale", required = true, example = "RU")
    @field:NotNull
    val locale: Locale,
    @ApiModelProperty(value = "subgroup code", required = true, example = "sentence_with_6_words")
    @field:NotBlank
    val subGroup: String,
    @field:NotNull
    @ApiModelProperty(value = "level", required = true, example = "1")
    val level: Int,
    @field:NotBlank
    @ApiModelProperty(value = "exercise name", required = true, example = "Пойми предложение из 6 слов из 18")
    val exerciseName: String,
    @field:NotNull
    @ApiModelProperty(value = "order number", required = true, example = "1")
    val orderNumber: Int,
    @ApiModelProperty(value = "sets of words for creating sentences", required = true)
    @field:NotNull
    val words: SetOfWords
) {
    fun toSeriesMatrixRecord() = SeriesMatrixRecord(
        level = level,
        code = subGroup,
        exerciseName = exerciseName,
        orderNumber = orderNumber,
        words = listOf(
            words.count?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY,
            words.objectDescription?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY,
            words.objectWord?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY,
            words.objectAction?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY,
            words.additionObjectDescription?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY,
            words.additionObject?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY
        )
    )
}
