package com.epam.brn.upload.csv.record

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonPropertyOrder(value = ["groupId", "name", "description"])
data class GroupRecord(
    @JsonProperty("groupId")
    val groupId: Long,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("description")
    val description: String
) : CsvRecord() {
    companion object {
        const val HEADER = "groupId, name, description"
    }
}
