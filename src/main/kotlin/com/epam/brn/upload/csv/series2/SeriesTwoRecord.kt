package com.epam.brn.upload.csv.series2

import com.fasterxml.jackson.annotation.JsonProperty

data class SeriesTwoRecord(
    @JsonProperty("level")
    val level: Int,
    @JsonProperty("code")
    val code: String,
    @JsonProperty("exerciseName")
    val exerciseName: String,
    @JsonProperty("orderNumber")
    val orderNumber: Int,
    @JsonProperty("words")
    val words: List<String>
) {
    companion object {
        const val FORMAT = "level,code,exerciseName,orderNumber,words"
    }
}
