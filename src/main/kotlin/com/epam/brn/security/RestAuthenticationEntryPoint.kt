package com.epam.brn.security

import com.epam.brn.dto.BaseResponseDto
import com.fasterxml.jackson.databind.ObjectMapper
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component("restAuthenticationEntryPoint")
class RestAuthenticationEntryPoint(
    @Qualifier("kotlinObjectMapper") private val kotlinObjectMapper: ObjectMapper
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        val baseResponseDto = BaseResponseDto(
            errors = listOf("Bad Credentials")
        )
        response.outputStream.println(kotlinObjectMapper.writeValueAsString(baseResponseDto))
    }
}
