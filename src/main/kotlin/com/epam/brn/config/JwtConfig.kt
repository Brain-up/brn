package com.epam.brn.config

import org.springframework.beans.factory.annotation.Value

class JwtConfig {
    @Value("\${security.jwt.uri:/brnlogin/**}")
    val uri: String = ""
    @Value("\${security.jwt.header:Authorization}")
    val header: String? = ""
    @Value("\${security.jwt.prefix:Bearer }")
    val prefix: String = ""
    @Value("\${security.jwt.expiration:#{24*60*60}}")
    val expiration: Long = 0
    @Value("\${security.jwt.secret:JwtSecretKey}")
    val secret: String = ""
}
