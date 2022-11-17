package com.epam.brn.dto.response

data class BrnResponse<T>(
    val data: T,
    val errors: List<Any> = emptyList(),
    val meta: List<Any> = emptyList()
)
