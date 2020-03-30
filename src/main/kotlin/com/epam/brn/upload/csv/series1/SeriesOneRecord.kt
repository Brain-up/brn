package com.epam.brn.upload.csv.series1

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
) {
    companion object {
        const val FORMAT = "level exerciseName orderNumber word audioFileName pictureFileName words wordType"
    }
}
