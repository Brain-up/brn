package com.epam.brn.security

import com.epam.brn.config.JwtConfig
import com.epam.brn.dto.AuthOutDto
import com.epam.brn.dto.LoginDto
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
    private val jwtConfig: JwtConfig,
    private val kotlinObjectMapper: ObjectMapper
) : UsernamePasswordAuthenticationFilter() {

    init {
        setRequiresAuthenticationRequestMatcher(AntPathRequestMatcher(jwtConfig.uri, "POST"))
    }

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        return try {
            val credentials = kotlinObjectMapper.readValue(request.inputStream, LoginDto::class.java)
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
        val expirationDate = Date(now + jwtConfig.expiration * 1000)
        val token: String = Jwts.builder()
            .setSubject(auth.name)
            .claim(
                "authorities", auth.authorities
                    .mapNotNull { it.authority }
            )
            .setIssuedAt(Date(now))
            .setExpiration(expirationDate)
            .signWith(SignatureAlgorithm.HS512, jwtConfig.secret.toByteArray())
            .compact()
        setHeaderToResponse(response, token, jwtConfig.expiration)
        response.addHeader(jwtConfig.header, jwtConfig.prefix + token)
    }

    private fun setHeaderToResponse(
        response: HttpServletResponse,
        token: String,
        expiresIn: Long
    ): HttpServletResponse {
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        val authOutDto = AuthOutDto(token, "Bearer", expiresIn)
        val responseBody = kotlinObjectMapper.writeValueAsString(authOutDto)
        response.writer.write(responseBody)
        return response
    }
}
