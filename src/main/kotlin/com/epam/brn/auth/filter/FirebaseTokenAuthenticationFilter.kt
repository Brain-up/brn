package com.epam.brn.auth.filter

import com.epam.brn.auth.model.UserAccountCredentials
import com.epam.brn.service.FirebaseUserService
import com.epam.brn.service.TokenHelperUtils
import com.epam.brn.service.UserAccountService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import org.apache.logging.log4j.kotlin.logger
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class FirebaseTokenAuthenticationFilter(
    private val brainUpUserDetailsService: UserDetailsService,
    private val firebaseUserService: FirebaseUserService,
    private val userAccountService: UserAccountService,
    private val firebaseAuth: FirebaseAuth,
    private val tokenHelperUtils: TokenHelperUtils
) : OncePerRequestFilter() {

    private val log = logger()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        verifyToken(request)
        filterChain.doFilter(request, response)
    }

    private fun verifyToken(request: HttpServletRequest) {
        val token: String? = tokenHelperUtils.getBearerToken(request)
        if (token.isNullOrEmpty()) {
            return
        }

        try {
            val decodedToken: FirebaseToken = firebaseAuth.verifyIdToken(token, true)
            try {
                val user: UserDetails = brainUpUserDetailsService.loadUserByUsername(decodedToken.email)
                setAuthentication(user, decodedToken, token, request)
            } catch (e: UsernameNotFoundException) {
                log.warn("User with email: ${decodedToken.email} doesn't exist: create it")
                val firebaseUserRecord = firebaseUserService.getUserByUuid(decodedToken.uid) ?: return
                val createdUser = userAccountService.createUser(firebaseUserRecord)
                val user: UserDetails = brainUpUserDetailsService.loadUserByUsername(createdUser.email)
                setAuthentication(user, decodedToken, token, request)
            }
        } catch (e: FirebaseAuthException) {
            log.error("Error while validate token: ${e.message}", e)
        } catch (e: Exception) {
            log.error("Error: ${e.message}", e)
        }
    }

    private fun setAuthentication(
        user: UserDetails,
        decodedToken: FirebaseToken,
        token: String,
        request: HttpServletRequest
    ) {
        val authentication = UsernamePasswordAuthenticationToken(
            user,
            UserAccountCredentials(decodedToken, token),
            user.authorities
        ).apply {
            details = WebAuthenticationDetailsSource().buildDetails(request)
        }
        SecurityContextHolder.getContext().authentication = authentication
    }
}
