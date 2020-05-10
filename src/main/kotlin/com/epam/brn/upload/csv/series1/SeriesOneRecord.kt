package com.epam.brn.upload.csv.series1

import com.fasterxml.jackson.annotation.JsonProperty

data class SeriesOneRecord(
    @JsonProperty("level")
    val level: Int,
    @JsonProperty("exerciseName")
    val exerciseName: String,
    @JsonProperty("words")
    val words: List<String>,
    @JsonProperty("noise")
    val noise: String
) {
    companion object {
        const val FORMAT = "level,exerciseName,words,noise"
    }
}
