package com.epam.brn.security

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.logger
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler

class CustomLogoutSuccessHandler : LogoutSuccessHandler {
    private val log = logger()
    override fun onLogoutSuccess(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, authentication: Authentication?) {
        if (authentication?.details != null) {
            try {
                httpServletRequest.session.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        log.info("User Successfully Logout")
        httpServletResponse.status = HttpServletResponse.SC_NO_CONTENT
    }
}
