package com.epam.brn.dto.request.exercise

import com.epam.brn.enums.Locale
import com.epam.brn.upload.csv.seriesMatrix.SeriesMatrixRecord
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.apache.commons.lang3.StringUtils
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ApiModel(value = "ExerciseSentencesCreateDto", description = "request dto for create exercise 'sentences")
data class ExerciseSentencesCreateDto(
    @field:NotNull
    val locale: Locale,
    @field:NotBlank
    val subGroup: String,
    @field:NotNull
    val level: Int,
    @field:NotBlank
    val exerciseName: String,
    @field:NotNull
    val orderNumber: Int,
    @ApiModelProperty(value = "sets of words for creating sentences", example = "дочь мама бабушка", required = true)
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
