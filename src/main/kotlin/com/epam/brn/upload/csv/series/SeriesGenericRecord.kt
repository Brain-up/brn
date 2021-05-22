package com.epam.brn.upload.csv.series

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["groupCode", "seriesId", "name", "description"])
data class SeriesGenericRecord(

    @JsonProperty("groupCode")
    val groupCode: String,

    @JsonProperty("level")
    val level: Int,

    @JsonProperty("type")
    val type: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("description")
    val description: String
) {
    companion object {
        const val FORMAT = "groupCode, level, type, name, description"
    }
}
