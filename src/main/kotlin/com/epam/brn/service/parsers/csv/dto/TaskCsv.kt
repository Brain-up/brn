package com.epam.brn.service.parsers.csv.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["exerciseId, serialNumber, word, fileName, words"])
class TaskCsv(

    @JsonProperty("exerciseId")
    val exerciseId: Long,

    @JsonProperty("serialNumber")
    val serialNumber: Long,

    @JsonProperty("word")
    val word: String,

    @JsonProperty("fileName")
    val fileName: String,

    @JsonProperty("words")
    val words: List<String>
)