package com.epam.brn.service.impl

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.UserAccountService
import org.apache.commons.lang3.StringUtils.isNotEmpty
import org.apache.logging.log4j.kotlin.logger
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class UserAccountServiceImpl(
    private val userAccountRepository: UserAccountRepository,
    private val authorityService: AuthorityService,
    private val passwordEncoder: PasswordEncoder
) : UserAccountService {

    private val log = logger()

    override fun findUserByName(firstName: String, lastName: String): UserAccountDto {
        return userAccountRepository
            .findUserAccountByFirstNameAndLastName(firstName, lastName)
            .map(UserAccount::toDto)
            .orElseThrow {
                log.warn("User $firstName $lastName is not found")
                UsernameNotFoundException("User: $firstName $lastName is not found")
            }
    }

    override fun addUser(userAccountDto: UserAccountDto): UserAccountDto {
        val existUser = userAccountRepository.findUserAccountByEmail(userAccountDto.email)
        existUser.ifPresent {
            throw IllegalArgumentException("The user already exists!")
        }

        val setOfAuthorities = getTheAuthoritySet(userAccountDto)
        val hashedPassword = getHashedPassword(userAccountDto)

        val userAccount = userAccountDto.toModel(hashedPassword)
        userAccount.authoritySet = setOfAuthorities
        return userAccountRepository.save(userAccount).toDto()
    }

    fun getHashedPassword(userAccountDto: UserAccountDto) = passwordEncoder.encode(userAccountDto.password)

    private fun getTheAuthoritySet(userAccountDto: UserAccountDto): MutableSet<Authority> {
        var authorityNames = userAccountDto.authorities ?: mutableSetOf()
        if (authorityNames.isEmpty())
            authorityNames = mutableSetOf("ROLE_USER")

        return authorityNames
            .filter(::isNotEmpty)
            .mapTo(mutableSetOf()) {
                authorityService.findAuthorityByAuthorityName(it)
            }
    }

    override fun save(userAccountDto: UserAccountDto): UserAccountDto {
        val hashedPassword = getHashedPassword(userAccountDto)
        val userAccountModel = userAccountDto.toModel(hashedPassword)
        return userAccountRepository.save(userAccountModel).toDto()
    }

    override fun findUserById(id: Long): UserAccountDto {
        return userAccountRepository.findUserAccountById(id)
            .map { it.toDto() }
            .orElseThrow { EntityNotFoundException("No user was found for id = $id") }
    }

    override fun getUserFromTheCurrentSession(): UserAccountDto {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name ?: getNameFromPrincipals(authentication)
        return findUserByEmail(email)
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
