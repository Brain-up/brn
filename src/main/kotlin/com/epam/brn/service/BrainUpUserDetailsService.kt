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
import java.util.Locale

@Service("brainUpUserDetailService")
class BrainUpUserDetailsService(
    private val userAccountRepository: UserAccountRepository,
) : UserDetailsService {
    @Volatile
    private var authUsersCache: Cache<String, UserDetails>? = null

    override fun loadUserByUsername(email: String): UserDetails {
        val cacheKey = email.lowercase(Locale.ROOT)
        authUsersCache().getIfPresent(cacheKey)?.let { return it }

        return userAccountRepository
            .findAuthenticationUserByEmail(email)
            .map { CustomUserDetails(it) }
            .orElseThrow { UsernameNotFoundException("User with email: $email doesn't exist") }
            .also { authUsersCache().put(cacheKey, it) }
    }

    private fun authUsersCache(): Cache<String, UserDetails> = authUsersCache
        ?: synchronized(this) {
            authUsersCache
                ?: Caffeine
                    .newBuilder()
                    .maximumSize(MAX_AUTH_USER_CACHE_SIZE)
                    .expireAfterWrite(AUTH_USER_CACHE_TTL)
                    .build<String, UserDetails>()
                    .also { authUsersCache = it }
        }

    companion object {
        private const val MAX_AUTH_USER_CACHE_SIZE = 10_000L
        private val AUTH_USER_CACHE_TTL: Duration = Duration.ofMinutes(5)
    }
}
