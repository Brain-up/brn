package com.epam.brn.service

import com.epam.brn.model.CustomUserDetails
import com.epam.brn.integration.repo.UserAccountRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service("brainUpUserDetailService")
class BrainUpUserDetailsService(private val userAccountRepository: UserAccountRepository) : UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        return userAccountRepository.findUserAccountByEmail(email)
            .map { CustomUserDetails(it) }
            .orElseThrow { UsernameNotFoundException("User with email: $email doesn't exist") }
    }
}
