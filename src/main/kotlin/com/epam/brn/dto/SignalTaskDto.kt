package com.epam.brn.dto

import com.fasterxml.jackson.annotation.JsonIgnore

data class SignalTaskDto(
    val id: Long? = null,
    @JsonIgnore
    val exerciseId: Long? = null,
    val name: String? = "",
    val url: String? = "",
    val frequency: Int? = null,
    val length: Int? = null
)
