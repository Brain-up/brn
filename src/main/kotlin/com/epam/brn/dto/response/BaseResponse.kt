package com.epam.brn.dto.response

data class BaseResponse<T>(
    val data: T,
    val errors: List<Any> = emptyList(),
    val meta: List<Any> = emptyList()
)
