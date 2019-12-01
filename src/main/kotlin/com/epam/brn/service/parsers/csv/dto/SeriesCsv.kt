package com.epam.brn.service.parsers.csv.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["groupId", "seriesId", "name", "description"])
data class SeriesCsv(
    @JsonProperty("groupId")
    val groupId: Long,

    @JsonProperty("seriesId")
    val seriesId: Long,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("description")
    val description: String
)