package com.epam.brn.upload.csv.record

import com.fasterxml.jackson.annotation.JsonProperty

data class SeriesOneRecord(

    @JsonProperty("level")
    val level: Int,

    @JsonProperty("exerciseName")
    val exerciseName: String,

    @JsonProperty("orderNumber")
    val orderNumber: Int,

    @JsonProperty("word")
    val word: String,

    @JsonProperty("audioFileName")
    val audioFileName: String,

    @JsonProperty("pictureFileName")
    val pictureFileName: String,

    @JsonProperty("words")
    val words: List<String>,

    @JsonProperty("wordType")
    val wordType: String
)
