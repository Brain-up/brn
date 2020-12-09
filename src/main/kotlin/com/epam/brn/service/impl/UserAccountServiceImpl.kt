package com.epam.brn.service.impl

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.TimeService
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
    private val passwordEncoder: PasswordEncoder,
    private val timeService: TimeService
) : UserAccountService {

    private val log = logger()

    override fun findUserByName(name: String): UserAccountDto {
        return userAccountRepository
            .findUserAccountByName(name)
            .map(UserAccount::toDto)
            .orElseThrow {
                log.warn("User with `$name` is not found")
                UsernameNotFoundException("User: `$name` is not found")
            }
    }

    override fun addUser(userAccountCreateRequest: UserAccountCreateRequest): UserAccountDto {
        val existUser = userAccountRepository.findUserAccountByEmail(userAccountCreateRequest.email)
        existUser.ifPresent {
            throw IllegalArgumentException("The user already exists!")
        }

        val setOfAuthorities = getTheAuthoritySet(userAccountCreateRequest)
        val hashedPassword = getHashedPassword(userAccountCreateRequest)

        val userAccount = userAccountCreateRequest.toModel(hashedPassword)
        userAccount.authoritySet = setOfAuthorities
        userAccount.created = timeService.now()
        userAccount.changed = timeService.now()
        return userAccountRepository.save(userAccount).toDto()
    }

    fun getHashedPassword(userAccountCreateRequest: UserAccountCreateRequest): String =
        passwordEncoder.encode(userAccountCreateRequest.password)

    private fun getTheAuthoritySet(userAccountCreateRequest: UserAccountCreateRequest): MutableSet<Authority> {
        var authorityNames = userAccountCreateRequest.authorities ?: mutableSetOf()
        if (authorityNames.isEmpty())
            authorityNames = mutableSetOf("ROLE_USER")

        return authorityNames
            .filter(::isNotEmpty)
            .mapTo(mutableSetOf()) {
                authorityService.findAuthorityByAuthorityName(it)
            }
    }

    override fun save(userAccountCreateRequest: UserAccountCreateRequest): UserAccountDto {
        val hashedPassword = getHashedPassword(userAccountCreateRequest)
        val userAccountModel = userAccountCreateRequest.toModel(hashedPassword)
        userAccountModel.changed = timeService.now()
        return userAccountRepository.save(userAccountModel).toDto()
    }

    override fun findUserById(id: Long): UserAccountDto {
        return userAccountRepository.findUserAccountById(id)
            .map { it.toDto() }
            .orElseThrow { EntityNotFoundException("No user was found for id = $id") }
    }

    override fun getUserFromTheCurrentSession(): UserAccountDto = getCurrentUser().toDto()

    fun getCurrentUser(): UserAccount {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name ?: getNameFromPrincipals(authentication)
        return userAccountRepository.findUserAccountByEmail(email)
            .orElseThrow { EntityNotFoundException("No user was found for email=$email") }
    }

    override fun getUsers(): List<UserAccountDto> =
        userAccountRepository.findAll().map { it.toDto() }

    override fun updateAvatarForCurrentUser(avatarUrl: String): UserAccountDto {
        val currentUserAccount = getCurrentUser()
        currentUserAccount.avatar = avatarUrl
        currentUserAccount.changed = timeService.now()
        return userAccountRepository.save(currentUserAccount).toDto()
    }

    override fun updateCurrentUser(userChangeRequest: UserAccountChangeRequest): UserAccountDto {
        val currentUserAccount = getCurrentUser()
        var changed = false
        if (!userChangeRequest.name.isNullOrEmpty()) {
            currentUserAccount.fullName = userChangeRequest.name
            changed = true
        }
        if (userChangeRequest.bornYear != null) {
            currentUserAccount.bornYear = userChangeRequest.bornYear
            changed = true
        }
        if (userChangeRequest.gender != null) {
            currentUserAccount.gender = userChangeRequest.gender.toString()
            changed = true
        }
        if (userChangeRequest.avatar != null) {
            currentUserAccount.avatar = userChangeRequest.avatar
            changed = true
        }
        return if (changed) {
            currentUserAccount.changed = timeService.now()
            userAccountRepository.save(currentUserAccount).toDto()
        } else
            currentUserAccount.toDto()
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
