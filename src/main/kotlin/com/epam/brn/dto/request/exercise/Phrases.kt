package com.epam.brn.dto.request.exercise

import com.epam.brn.validator.WithoutPunctuation
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotEmpty

@Schema(name = "Phrases", description = "Phrases for creating exercise. Phrases should not contain punctuation marks.")
data class Phrases(
    @Schema(description = "Short phrases", required = true, example = "Мамочка идёт")
    @field:NotEmpty
    @field:WithoutPunctuation
    val shortPhrase: String,
    @Schema(description = "Long phrases", required = true, example = "Мамочка быстро идёт в магазин")
    @field:NotEmpty
    @field:WithoutPunctuation
    val longPhrase: String
) {
    fun toList(): List<String> = listOf(shortPhrase, longPhrase)
    fun toListWithDot(): List<String> = listOf(shortPhrase.plus("."), longPhrase)
}
