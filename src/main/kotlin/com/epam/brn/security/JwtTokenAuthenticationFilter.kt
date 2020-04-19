package com.epam.brn.security

import com.epam.brn.config.JwtConfig
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtTokenAuthenticationFilter(private val jwtConfig: JwtConfig) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val header = request.getHeader(jwtConfig.header)
        if (header.isNullOrEmpty() || !header.startsWith(jwtConfig.prefix)) {
            filterChain.doFilter(request, response)
            return
        }
        val token: String = header.replace(jwtConfig.prefix, "")
        try {
            val claims: Claims = Jwts.parser()
                .setSigningKey(jwtConfig.secret.toByteArray())
                .parseClaimsJws(token)
                .body
            val username: String = claims.subject
            username.let {
                @Suppress("UNCHECKED_CAST")
                val authorities = claims["authorities"] as? List<String>
                val simpleGrandAuthorities = authorities.orEmpty()
                    .map { authority -> SimpleGrantedAuthority(authority) }
                val auth = UsernamePasswordAuthenticationToken(it, null, simpleGrandAuthorities)
                SecurityContextHolder.getContext().authentication = auth
            }
        } catch (e: Exception) {
            SecurityContextHolder.clearContext()
        }
        filterChain.doFilter(request, response)
    }
}
