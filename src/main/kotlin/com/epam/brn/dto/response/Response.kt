package com.epam.brn.dto.response

data class Response<T>(
    val data: T,
    val errors: List<Any> = emptyList(),
    val meta: List<Any> = emptyList()
)
