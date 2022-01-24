package com.epam.brn.dto.response

data class BaseResponse(
    val data: List<Any> = emptyList(),
    val errors: List<Any> = emptyList(),
    val meta: List<Any> = emptyList()
)

data class BaseSingleObjectResponse(
    val data: Any,
    val errors: List<Any> = emptyList(),
    val meta: List<Any> = emptyList()
)
