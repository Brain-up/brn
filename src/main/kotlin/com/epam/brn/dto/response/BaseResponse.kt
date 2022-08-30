package com.epam.brn.dto.response

data class Response<T>(
    val data: T,
    val errors: List<Any> = emptyList(),
    val meta: List<Any> = emptyList()
)

@Deprecated("use Response")
data class BaseResponse(
    val data: List<Any> = emptyList(),
    val errors: List<Any> = emptyList(),
    val meta: List<Any> = emptyList()
)

@Deprecated("use Response")
data class BaseSingleObjectResponse(
    val data: Any,
    val errors: List<Any> = emptyList(),
    val meta: List<Any> = emptyList()
)
