package com.epam.brn.dto

import springfox.documentation.spring.web.json.Json

data class BaseResponseDto(
    val data: Json? = Json(""),
    val errors: Json? = Json(""),
    val meta: Json? = Json("")
)