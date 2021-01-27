package com.epam.brn.upload.csv.group

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["groupId", "locale", "name", "description"])
data class GroupRecord(
    @JsonProperty("groupId")
    val groupId: Long,

    @JsonProperty("locale")
    val locale: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("description")
    val description: String
) {
    companion object {
        const val FORMAT = "groupId, locale, name, description"
    }
}
