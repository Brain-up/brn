package com.epam.brn.dto.request.exercise

import com.epam.brn.validator.WithoutPunctuation
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotEmpty

@ApiModel(value = "Phrases", description = "Phrases for creating exercise. Phrases should not contain punctuation marks.")
data class Phrases(
    @ApiModelProperty(value = "Short phrases", required = true, example = "Мамочка идёт", position = 1)
    @field:NotEmpty
    @field:WithoutPunctuation
    val shortPhrase: String,
    @ApiModelProperty(value = "Long phrases", required = true, example = "Мамочка быстро идёт в магазин", position = 2)
    @field:NotEmpty
    @field:WithoutPunctuation
    val longPhrase: String
) {
    fun toList(): List<String> = listOf(shortPhrase, longPhrase)
    fun toListWithDot(): List<String> = listOf(shortPhrase.plus("."), longPhrase)
}
