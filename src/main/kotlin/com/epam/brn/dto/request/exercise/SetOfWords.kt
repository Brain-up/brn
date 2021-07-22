package com.epam.brn.dto.request.exercise

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.apache.commons.lang3.StringUtils

@ApiModel(value = "SetOfWords", description = "Sets of words for creating sentences")
data class SetOfWords(
    @ApiModelProperty(value = "Words with wordType=COUNT", example = "[пять, шесть, семь]")
    val count: List<String>?,
    @ApiModelProperty(value = "Words with wordType=OBJECT_DESCRIPTION", example = "[бабушек, дедушек, девушек]")
    val objectDescription: List<String>?,
    @ApiModelProperty(value = "Words with wordType=OBJECT", example = "[купили, слепили, продали]")
    val objectWord: List<String>?,
    @ApiModelProperty(value = "Words with wordType=OBJECT_ACTION", example = "[тёте, дяде, папе]")
    val objectAction: List<String>?,
    @ApiModelProperty(value = "Words with wordType=ADDITION_OBJECT_DESCRIPTION", example = "[красные, белые, желтые]")
    val additionObjectDescription: List<String>?,
    @ApiModelProperty(value = "Words with wordType=ADDITION_OBJECT", example = "[шторы, пышки, вилки]")
    val additionObject: List<String>?
) {
    fun toRecordList(): List<String> = listOf(
        count?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY,
        objectDescription?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY,
        objectWord?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY,
        objectAction?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY,
        additionObjectDescription?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY,
        additionObject?.joinToString(separator = StringUtils.SPACE) ?: StringUtils.EMPTY
    )

    fun toFlattenList(): List<String> = listOf(
        count.orEmpty(),
        objectDescription.orEmpty(),
        objectWord.orEmpty(),
        objectAction.orEmpty(),
        additionObjectDescription.orEmpty(),
        additionObject.orEmpty(),
    ).flatten()
}
