package com.epam.brn.upload.csv.record

import com.fasterxml.jackson.annotation.JsonProperty

data class SeriesTwoRecord(
    @JsonProperty("level")
    val level: Int,
    @JsonProperty("exerciseName")
    val exerciseName: String,
    @JsonProperty("orderNumber")
    val orderNumber: Int,
    @JsonProperty("words")
    val words: List<String>
)
