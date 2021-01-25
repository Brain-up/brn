package com.epam.brn.upload.csv.series4

import com.fasterxml.jackson.annotation.JsonProperty

data class SeriesFourRecord(
    @JsonProperty("level")
    val level: Int,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("exerciseName")
    val exerciseName: String,
    @JsonProperty("phrases")
    val phrases: List<String>,
    @JsonProperty("noiseLevel")
    val noiseLevel: Int,
    @JsonProperty("noiseUrl")
    val noiseUrl: String
) {
    companion object {
        const val FORMAT = "level,code,exerciseName,phrases,noiseLevel,noiseUrl"
    }
}
