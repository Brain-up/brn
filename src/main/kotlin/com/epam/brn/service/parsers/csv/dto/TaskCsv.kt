package com.epam.brn.service.parsers.csv.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["exerciseId, orderNumber, word, audioFileName, pictureFileName , words"])
data class TaskCsv(

    @JsonProperty("exerciseId")
    val exerciseId: Long,

    @JsonProperty("orderNumber")
    val orderNumber: Int,

    @JsonProperty("word")
    val word: String,

    @JsonProperty("audioFileName")
    val audioFileName: String,

    @JsonProperty("pictureFileName")
    val pictureFileName: String,

    @JsonProperty("words")
    val words: List<String>
)