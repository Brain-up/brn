package com.epam.brn.upload.csv.seriesWords

import com.fasterxml.jackson.annotation.JsonProperty

data class SeriesWordsRecord(
    @JsonProperty("level")
    val level: Int,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("exerciseName")
    val exerciseName: String,
    @JsonProperty("words")
    val words: List<String>,
    @JsonProperty("noiseLevel")
    val noiseLevel: Int,
    @JsonProperty("noiseUrl")
    val noiseUrl: String
) {
    companion object {
        const val FORMAT = "level,code,exerciseName,words,noiseLevel,noiseUrl"
    }
}
