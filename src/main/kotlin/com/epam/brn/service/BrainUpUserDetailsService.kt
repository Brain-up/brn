package com.epam.brn.service

import com.epam.brn.auth.model.CustomUserDetails
import com.epam.brn.repo.UserAccountRepository
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale

@Service("brainUpUserDetailService")
class BrainUpUserDetailsService(
    private val userAccountRepository: UserAccountRepository,
) : UserDetailsService {
    @Volatile
    private var authUsersCache: Cache<String, CachedAuthenticationUser>? = null

    override fun loadUserByUsername(email: String): UserDetails = loadUserByUsername(email, findAuthenticationStateChangedAt(email))

    fun loadUserByUsername(
        email: String,
        authStateChangedAt: LocalDateTime?,
    ): UserDetails {
        val cacheKey = email.lowercase(Locale.ROOT)
        authUsersCache().getIfPresent(cacheKey)?.let { cachedUser ->
            if (authStateChangedAt == null || cachedUser.authStateChangedAt == authStateChangedAt)
                return cachedUser.userDetails
            authUsersCache().invalidate(cacheKey)
        }

        val cachedUser =
            userAccountRepository
                .findAuthenticationUserByEmail(email)
                .map {
                    CachedAuthenticationUser(
                        userDetails = CustomUserDetails(it),
                        authStateChangedAt = it.authStateChanged,
                    )
                }.orElseThrow { UsernameNotFoundException("User with email: $email doesn't exist") }
        authUsersCache().put(cacheKey, cachedUser)
        return cachedUser.userDetails
    }

    fun findAuthenticationStateChangedAt(email: String): LocalDateTime? = userAccountRepository
        .findAuthenticationStateChangedAtByEmail(email)

    fun evictCachedUser(email: String) {
        authUsersCache().invalidate(email.lowercase(Locale.ROOT))
    }

    private fun authUsersCache(): Cache<String, CachedAuthenticationUser> = authUsersCache
        ?: synchronized(this) {
            authUsersCache
                ?: Caffeine
                    .newBuilder()
                    .maximumSize(MAX_AUTH_USER_CACHE_SIZE)
                    .expireAfterWrite(AUTH_USER_CACHE_TTL)
                    .build<String, CachedAuthenticationUser>()
                    .also { authUsersCache = it }
        }

    private data class CachedAuthenticationUser(
        val userDetails: UserDetails,
        val authStateChangedAt: LocalDateTime,
    )

    companion object {
        private const val MAX_AUTH_USER_CACHE_SIZE = 10_000L
        private val AUTH_USER_CACHE_TTL: Duration = Duration.ofMinutes(5)
    }
}
