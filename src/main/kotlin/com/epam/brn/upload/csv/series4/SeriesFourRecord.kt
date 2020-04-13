package com.epam.brn.upload.csv.series4

import com.fasterxml.jackson.annotation.JsonProperty

data class SeriesFourRecord(
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
