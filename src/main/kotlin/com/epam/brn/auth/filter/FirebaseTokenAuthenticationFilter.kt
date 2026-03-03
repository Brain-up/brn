package com.epam.brn.auth.filter

import com.epam.brn.auth.model.UserAccountCredentials
import com.epam.brn.service.FirebaseUserService
import com.epam.brn.service.TokenHelperUtils
import com.epam.brn.service.UserAccountService
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
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
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class FirebaseTokenAuthenticationFilter(
    private val brainUpUserDetailsService: UserDetailsService,
    private val firebaseUserService: FirebaseUserService,
    private val userAccountService: UserAccountService,
    private val firebaseAuth: FirebaseAuth,
    private val tokenHelperUtils: TokenHelperUtils,
) : OncePerRequestFilter() {
    private val log = logger()
    private val clock: Clock = Clock.systemUTC()

    @Volatile
    private var verifiedTokensCache: Cache<String, CachedVerifiedToken>? = null

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (SecurityContextHolder.getContext().authentication != null) {
            filterChain.doFilter(request, response)
            return
        }
        verifyToken(request)
        filterChain.doFilter(request, response)
    }

    private fun verifyToken(request: HttpServletRequest) {
        val token = tokenHelperUtils.getBearerToken(request) ?: return
        try {
            val decodedToken = getVerifiedToken(token)
            try {
                val user: UserDetails = brainUpUserDetailsService.loadUserByUsername(decodedToken.email)
                val authentication =
                    UsernamePasswordAuthenticationToken(
                        user,
                        UserAccountCredentials(decodedToken, token),
                        user.authorities,
                    )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: UsernameNotFoundException) {
                log.warn("User with email: ${decodedToken.email} doesn't exist: create it")
                val firebaseUserRecord = firebaseUserService.getUserByUuid(decodedToken.uid)
                if (firebaseUserRecord != null) {
                    val createUser = userAccountService.createUser(firebaseUserRecord)
                    val user: UserDetails = brainUpUserDetailsService.loadUserByUsername(createUser.email)
                    val authentication =
                        UsernamePasswordAuthenticationToken(
                            user,
                            UserAccountCredentials(decodedToken, token),
                            user.authorities,
                        )
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (e: FirebaseAuthException) {
            log.error("Error while validate token: ${e.message}", e)
        } catch (e: Exception) {
            log.error("Error: ${e.message}", e)
        }
    }

    private fun getVerifiedToken(token: String): FirebaseToken {
        val tokenHash = hashToken(token)
        verifiedTokensCache().getIfPresent(tokenHash)?.let { cachedToken ->
            if (!cachedToken.isExpired(clock)) {
                return cachedToken.decodedToken
            }
            verifiedTokensCache().invalidate(tokenHash)
        }

        return firebaseAuth
            .verifyIdToken(token, true)
            .also { cacheVerifiedToken(tokenHash, it) }
    }

    private fun cacheVerifiedToken(
        tokenHash: String,
        decodedToken: FirebaseToken,
    ) {
        val now = clock.instant()
        val tokenExpiresAt = getTokenExpiresAt(decodedToken) ?: return
        if (!tokenExpiresAt.isAfter(now)) return

        verifiedTokensCache().put(
            tokenHash,
            CachedVerifiedToken(
                decodedToken = decodedToken,
                expiresAt = minOf(tokenExpiresAt, now.plus(MAX_VERIFIED_TOKEN_CACHE_TTL)),
            ),
        )
    }

    private fun verifiedTokensCache(): Cache<String, CachedVerifiedToken> = verifiedTokensCache
        ?: synchronized(this) {
            verifiedTokensCache
                ?: Caffeine
                    .newBuilder()
                    .maximumSize(MAX_VERIFIED_TOKEN_CACHE_SIZE)
                    .expireAfterWrite(MAX_VERIFIED_TOKEN_CACHE_TTL)
                    .build<String, CachedVerifiedToken>()
                    .also { verifiedTokensCache = it }
        }

    private fun hashToken(token: String): String = MessageDigest
        .getInstance(TOKEN_HASH_ALGORITHM)
        .digest(token.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }

    private fun getTokenExpiresAt(decodedToken: FirebaseToken): Instant? {
        val expirationClaim = decodedToken.claims[TOKEN_EXPIRATION_CLAIM]
        return when (expirationClaim) {
            is Number -> Instant.ofEpochSecond(expirationClaim.toLong())
            is String -> expirationClaim.toLongOrNull()?.let(Instant::ofEpochSecond)
            else -> null
        }
    }

    private data class CachedVerifiedToken(
        val decodedToken: FirebaseToken,
        val expiresAt: Instant,
    ) {
        fun isExpired(clock: Clock): Boolean = !expiresAt.isAfter(clock.instant())
    }

    companion object {
        private const val TOKEN_EXPIRATION_CLAIM = "exp"
        private const val TOKEN_HASH_ALGORITHM = "SHA-256"
        private const val MAX_VERIFIED_TOKEN_CACHE_SIZE = 10_000L
        private val MAX_VERIFIED_TOKEN_CACHE_TTL: Duration = Duration.ofMinutes(5)
    }
}
