package com.epam.brn.upload.csv.subgroup

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["seriesType, level, picture, name, description"])
data class SubgroupGenericRecord(
    @JsonProperty("seriesType")
    val seriesType: String,

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
        const val FORMAT = "seriesType, level, code, name, description"
    }
}
