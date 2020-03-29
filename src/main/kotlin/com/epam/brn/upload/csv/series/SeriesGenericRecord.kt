package com.epam.brn.upload.csv.series

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["groupId", "seriesId", "name", "description"])
data class SeriesGenericRecord(
    @JsonProperty("groupId")
    val groupId: Long,

    @JsonProperty("seriesId")
    val seriesId: Long,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("description")
    val description: String
) {
    companion object {
        const val FORMAT = "groupId, seriesId, name, description"
    }
}
