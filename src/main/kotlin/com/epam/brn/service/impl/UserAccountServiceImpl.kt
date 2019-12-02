package com.epam.brn.service.impl

import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.UserAccountService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserAccountServiceImpl(private val userAccountRepository: UserAccountRepository) : UserAccountService {

    private val log = logger()

    override fun findUserByName(name: String): UserAccount {
        return userAccountRepository.findByUserName(name)
            .orElseThrow {
                log.warn("User $name is not found")
                UsernameNotFoundException("User $name is not found")
            }

    }
}