package com.epam.brn.service.impl

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.request.UserAccountRequest
import com.epam.brn.dto.response.UserAccountResponse
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

    override fun findUserByName(name: String): UserAccountResponse {
        return userAccountRepository
            .findUserAccountByName(name)
            .map(UserAccount::toDto)
            .orElseThrow {
                log.warn("User with `$name` is not found")
                UsernameNotFoundException("User: `$name` is not found")
            }
    }

    override fun addUser(userAccountRequest: UserAccountRequest): UserAccountResponse {
        val existUser = userAccountRepository.findUserAccountByEmail(userAccountRequest.email)
        existUser.ifPresent {
            throw IllegalArgumentException("The user already exists!")
        }

        val setOfAuthorities = getTheAuthoritySet(userAccountRequest)
        val hashedPassword = getHashedPassword(userAccountRequest)

        val userAccount = userAccountRequest.toModel(hashedPassword)
        userAccount.authoritySet = setOfAuthorities
        return userAccountRepository.save(userAccount).toDto()
    }

    fun getHashedPassword(userAccountRequest: UserAccountRequest) = passwordEncoder.encode(userAccountRequest.password)

    private fun getTheAuthoritySet(userAccountRequest: UserAccountRequest): MutableSet<Authority> {
        var authorityNames = userAccountRequest.authorities ?: mutableSetOf()
        if (authorityNames.isEmpty())
            authorityNames = mutableSetOf("ROLE_USER")

        return authorityNames
            .filter(::isNotEmpty)
            .mapTo(mutableSetOf()) {
                authorityService.findAuthorityByAuthorityName(it)
            }
    }

    override fun save(userAccountRequest: UserAccountRequest): UserAccountResponse {
        val hashedPassword = getHashedPassword(userAccountRequest)
        val userAccountModel = userAccountRequest.toModel(hashedPassword)
        return userAccountRepository.save(userAccountModel).toDto()
    }

    override fun findUserById(id: Long): UserAccountResponse {
        return userAccountRepository.findUserAccountById(id)
            .map { it.toDto() }
            .orElseThrow { EntityNotFoundException("No user was found for id = $id") }
    }

    override fun getUserFromTheCurrentSession(): UserAccountResponse {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name ?: getNameFromPrincipals(authentication)
        return findUserByEmail(email)
    }

    override fun removeUserWithId(id: Long) {
        findUserById(id)
        userAccountRepository.deleteById(id)
    }

    override fun getUsers(): List<UserAccountResponse> =
        userAccountRepository.findAll().map { it.toDto() }

    override fun updateAvatarCurrentUser(avatarUrl: String): UserAccountResponse {
        val currentUser = getUserFromTheCurrentSession()
        val currentUserAccount = (userAccountRepository.findUserAccountById(currentUser.id!!)).get()
        currentUserAccount.avatar = avatarUrl
        return userAccountRepository.save(currentUserAccount).toDto()
    }

    private fun getNameFromPrincipals(authentication: Authentication): String {
        val principal = authentication.principal
        if (principal is UserDetails)
            return principal.username
        if (principal is Principal)
            return principal.name

        throw EntityNotFoundException("There is no user in the session")
    }

    override fun findUserByEmail(email: String): UserAccountResponse {
        return userAccountRepository.findUserAccountByEmail(email)
            .map { it.toDto() }
            .orElseThrow { EntityNotFoundException("No user was found for email=$email") }
    }
}
