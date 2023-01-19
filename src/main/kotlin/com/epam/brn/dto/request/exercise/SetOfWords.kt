package com.epam.brn.dto.request.exercise

import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.StringUtils

@Schema(name = "SetOfWords", description = "Sets of words for creating sentences")
data class SetOfWords(
    @Schema(description = "Words with wordType=COUNT", example = "[пять, шесть, семь]")
    val count: List<String> = emptyList(),
    @Schema(description = "Words with wordType=OBJECT_DESCRIPTION", example = "[бабушек, дедушек, девушек]")
    val objectDescription: List<String> = emptyList(),
    @Schema(description = "Words with wordType=OBJECT", example = "[купили, слепили, продали]")
    val objectWord: List<String> = emptyList(),
    @Schema(description = "Words with wordType=OBJECT_ACTION", example = "[тёте, дяде, папе]")
    val objectAction: List<String> = emptyList(),
    @Schema(description = "Words with wordType=ADDITION_OBJECT_DESCRIPTION", example = "[красные, белые, желтые]")
    val additionObjectDescription: List<String> = emptyList(),
    @Schema(description = "Words with wordType=ADDITION_OBJECT", example = "[шторы, пышки, вилки]")
    val additionObject: List<String> = emptyList()
) {
    fun toRecordList(): List<String> = listOf(
        count.joinToString(separator = StringUtils.SPACE),
        objectDescription.joinToString(separator = StringUtils.SPACE),
        objectWord.joinToString(separator = StringUtils.SPACE),
        objectAction.joinToString(separator = StringUtils.SPACE),
        additionObjectDescription.joinToString(separator = StringUtils.SPACE),
        additionObject.joinToString(separator = StringUtils.SPACE)
    )

    fun toFlattenList(): List<String> = listOf(
        count,
        objectDescription,
        objectWord,
        objectAction,
        additionObjectDescription,
        additionObject,
    ).flatten()
}
