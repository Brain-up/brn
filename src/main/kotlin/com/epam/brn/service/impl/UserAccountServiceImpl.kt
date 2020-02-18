package com.epam.brn.service.impl

import com.epam.brn.constant.BrnRoles.AUTH_ROLE_USER
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.AuthorityService
import com.epam.brn.service.UserAccountService
import java.security.Principal
import org.apache.commons.lang3.StringUtils.isNotEmpty
import org.apache.logging.log4j.kotlin.logger
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserAccountServiceImpl(
    private val userAccountRepository: UserAccountRepository,
    private val authorityService: AuthorityService,
    private val passwordEncoder: PasswordEncoder
) : UserAccountService {

    private val log = logger()

    override fun findUserByName(name: String): UserAccountDto {
        return userAccountRepository
            .findUserAccountByUserName(name)
            .map(UserAccount::toDto)
            .orElseThrow {
                log.warn("User $name is not found")
                UsernameNotFoundException("User with username: $name is not found")
            }
    }

    override fun addUser(userAccountDto: UserAccountDto): UserAccountDto {
        val setOfAuthorities = getTheAuthoritySet(userAccountDto)
        val hashedPassword = getHashedPassword(userAccountDto)

        userAccountDto.password = hashedPassword
        val userAccount = userAccountDto.toModel()
        userAccount.authoritySet = setOfAuthorities
        return userAccountRepository.save(userAccount).toDto()
    }

    private fun getHashedPassword(userAccountDto: UserAccountDto) = passwordEncoder.encode(userAccountDto.password)

    private fun getTheAuthoritySet(userAccountDto: UserAccountDto): MutableSet<Authority> {
        var authorityNames = userAccountDto.authorities ?: mutableSetOf()
        if (authorityNames.isEmpty())
            authorityNames = mutableSetOf(AUTH_ROLE_USER)

        return authorityNames
            .filter(::isNotEmpty)
            .mapTo(mutableSetOf()) {
                authorityService.findAuthorityByAuthorityName(it)
            }
    }

    override fun save(userAccountDto: UserAccountDto): UserAccountDto {
        val userAccountModel = userAccountDto.toModel()
        return userAccountRepository.save(userAccountModel).toDto()
    }

    override fun findUserById(id: Long): UserAccountDto {
        return userAccountRepository.findUserAccountById(id)
            .map { it.toDto() }
            .orElseThrow { EntityNotFoundException("No user was found for id = $id") }
    }

    override fun getUserFromTheCurrentSession(): UserAccountDto {
        val authentication = SecurityContextHolder.getContext().authentication
        val userName = authentication.name ?: getNameFromPrincipals(authentication)
        return findUserByName(userName)
    }

    override fun removeUserWithId(id: Long) {
        findUserById(id)
        userAccountRepository.deleteById(id)
    }

    private fun getNameFromPrincipals(authentication: Authentication): String {
        val principal = authentication.principal
        if (principal is UserDetails)
            return principal.username
        if (principal is Principal)
            return principal.name

        throw EntityNotFoundException("There is no user in the session")
    }

    override fun findUserByEmail(email: String): UserAccountDto {
        return userAccountRepository.findUserAccountByEmail(email)
            .map { it.toDto() }
            .orElseThrow { EntityNotFoundException("No user was found for email=$email") }
    }
}
