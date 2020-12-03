package com.epam.brn.upload.csv.subgroup

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["seriesId, level, picture, name, description"])
data class SubgroupGenericRecord(
    @JsonProperty("seriesId")
    val seriesId: Long,

    @JsonProperty("level")
    val level: Int,

    @JsonProperty("code")
    val code: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("description")
    val description: String
) {
    companion object {
        const val FORMAT = "seriesId, level, code, name, description"
    }
}
