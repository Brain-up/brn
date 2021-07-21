package com.epam.brn.dto.request.exercise

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "SetOfWords", description = "sets of words for creating sentences")
data class SetOfWords(
    @ApiModelProperty(value = "words with wordType=COUNT", example = "[пять, шесть, семь]")
    val count: List<String>?,
    @ApiModelProperty(value = "words with wordType=OBJECT_DESCRIPTION", example = "[бабушек, дедушек, девушек]")
    val objectDescription: List<String>?,
    @ApiModelProperty(value = "words with wordType=OBJECT", example = "[купили, слепили, продали]")
    val objectWord: List<String>?,
    @ApiModelProperty(value = "words with wordType=OBJECT_ACTION", example = "[тёте, дяде, папе]")
    val objectAction: List<String>?,
    @ApiModelProperty(value = "words with wordType=ADDITION_OBJECT_DESCRIPTION", example = "[красные, белые, желтые]")
    val additionObjectDescription: List<String>?,
    @ApiModelProperty(value = "words with wordType=ADDITION_OBJECT", example = "[шторы, пышки, вилки]")
    val additionObject: List<String>?
)
