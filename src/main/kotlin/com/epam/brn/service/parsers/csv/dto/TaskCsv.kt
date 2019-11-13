package com.epam.brn.service.parsers.csv.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["exerciseId, orderNumber, word, fileName, words"])
class TaskCsv(

    @JsonProperty("exerciseId")
    val exerciseId: Long,

    @JsonProperty("orderNumber")
    val orderNumber: Int,

    @JsonProperty("word")
    val word: String,

    @JsonProperty("fileName")
    val fileName: String,

    @JsonProperty("words")
    val words: List<String>
)