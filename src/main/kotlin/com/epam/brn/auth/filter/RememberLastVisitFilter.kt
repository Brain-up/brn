package com.epam.brn.auth.filter

import com.epam.brn.service.UserAccountService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class RememberLastVisitFilter(
    private val userAccountService: UserAccountService,
) : OncePerRequestFilter() {
    private val log = logger()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        markVisit()
        filterChain.doFilter(request, response)
    }

    private fun markVisit() {
        try {
            if (SecurityContextHolder.getContext().authentication != null) userAccountService.markVisitForCurrentUser()
        } catch (e: Exception) {
            log.error("Error: ${e.message}", e)
        }
    }
}
