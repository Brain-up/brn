package com.epam.brn.service.parsers.csv.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["seriesId, level, name, description"])
data class ExerciseCsv(
    @JsonProperty("exerciseId")
    val exerciseId: Long,

    @JsonProperty("seriesId")
    val seriesId: Long,

    @JsonProperty("level")
    val level: Short,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("description")
    val description: String
)
