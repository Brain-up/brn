package com.epam.brn.upload.csv.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["groupId", "name", "description"])
data class GroupCsv(
    @JsonProperty("groupId")
    val groupId: Long,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("description")
    val description: String
)
