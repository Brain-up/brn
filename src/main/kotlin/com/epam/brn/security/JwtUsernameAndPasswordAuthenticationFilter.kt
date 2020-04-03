package com.epam.brn.security

import com.epam.brn.dto.JwtConfig
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.io.IOException
import java.util.Date
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

class JwtUsernameAndPasswordAuthenticationFilter(
    private val authManager: AuthenticationManager,
    private val jwtConfig: JwtConfig
) : UsernamePasswordAuthenticationFilter() {

    init {
        setRequiresAuthenticationRequestMatcher(AntPathRequestMatcher(jwtConfig.uri, "POST"))
    }

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        return try {
            val credentials = ObjectMapper().readValue(request.inputStream, UserCredentials::class.java)
            val authToken = UsernamePasswordAuthenticationToken(
                credentials.username, credentials.password, emptyList()
            )
            authManager.authenticate(authToken)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        auth: Authentication
    ) {
        val now = System.currentTimeMillis()
        val token: String = Jwts.builder()
            .setSubject(auth.name)
            .claim(
                "authorities", auth.authorities
                    .mapNotNull { it.authority }
            )
            .setIssuedAt(Date(now))
            .setExpiration(Date(now + jwtConfig.expiration * 1000))
            .signWith(SignatureAlgorithm.HS512, jwtConfig.secret.toByteArray())
            .compact()
        response.addHeader(jwtConfig.header, jwtConfig.prefix + token)
    }

    private data class UserCredentials(
        val username: String? = null,
        val password: String? = null
    )
}
