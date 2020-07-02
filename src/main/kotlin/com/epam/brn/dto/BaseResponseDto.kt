package com.epam.brn.dto

import java.util.ArrayList

data class BaseResponseDto(
    val data: List<Any> = emptyList(),
    val errors: List<Any> = emptyList(),
    val meta: List<Any> = emptyList()
)

data class BaseSingleObjectResponseDto(
    val data: Any,
    val errors: List<Any> = emptyList(),
    val meta: List<Any> = emptyList()
)

data class ApiError(val errors: ArrayList<String>)
