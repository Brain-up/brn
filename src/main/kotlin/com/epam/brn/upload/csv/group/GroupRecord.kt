package com.epam.brn.upload.csv.group

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["code", "locale", "name", "description"])
data class GroupRecord(

    @JsonProperty("code")
    val code: String,

    @JsonProperty("locale")
    val locale: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("description")
    val description: String
) {
    companion object {
        const val FORMAT = "code, locale, name, description"
    }
}
