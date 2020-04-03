package com.epam.brn.dto

import org.springframework.beans.factory.annotation.Value

data class JwtConfig(
    @Value("\${security.jwt.uri:/auth/**}")
    val uri: String = "",
    @Value("\${security.jwt.header:Authorization}")
    val header: String? = "",
    @Value("\${security.jwt.prefix:Bearer }")
    val prefix: String = "",
    @Value("\${security.jwt.expiration:#{24*60*60}}")
    val expiration: Int = 0,
    @Value("\${security.jwt.secret:JwtSecretKey}")
    val secret: String = ""
)
