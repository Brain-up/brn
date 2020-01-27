package com.epam.brn.service

import com.epam.brn.model.CustomUserDetails
import com.epam.brn.repo.UserAccountRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service("brainUpUserDetailService")
class BrainUpUserDetailsService(private val userAccountRepository: UserAccountRepository) : UserDetailsService {
    override fun loadUserByUsername(userName: String): UserDetails {
        return userAccountRepository.findByUserName(userName)
            .map { CustomUserDetails(it) }
            .orElseThrow { UsernameNotFoundException("User with username: $userName doesn't exist") }
    }
}
