package com.epam.brn.service.impl

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.dto.response.UserWithAnalyticsDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.model.Headphones
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.HeadphonesService
import com.epam.brn.service.TimeService
import com.epam.brn.service.UserAccountService
import org.apache.commons.lang3.StringUtils.isNotEmpty
import org.apache.logging.log4j.kotlin.logger
import org.springframework.data.domain.Pageable
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
    private val timeService: TimeService,
    private val headphonesService: HeadphonesService
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

    override fun findUserEntityById(id: Long): UserAccount {
        return userAccountRepository.findUserAccountById(id)
            .orElseThrow { EntityNotFoundException("No user was found for id = $id") }
    }

    override fun getAllHeadphonesForUser(userId: Long) = headphonesService.getAllHeadphonesForUser(userId)

    override fun getAllHeadphonesForCurrentUser() = getCurrentUser().headphones.map(Headphones::toDto).toSet()

    override fun getUserFromTheCurrentSession(): UserAccountDto = getCurrentUser().toDto()

    override fun getCurrentUser(): UserAccount {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name ?: getNameFromPrincipals(authentication)
        return userAccountRepository.findUserAccountByEmail(email)
            .orElseThrow { EntityNotFoundException("No user was found for email=$email") }
    }

    override fun getUsers(pageable: Pageable): List<UserAccountDto> =
        userAccountRepository.findAll().map { it.toDto() }

    override fun getUsersWithAnalytics(pageable: Pageable): List<UserWithAnalyticsDto> {
        val users = userAccountRepository.findAll().map { it.toAnalyticsDto() }
        // todo fill user models with analytics and write tests
        return users
    }

    override fun updateAvatarForCurrentUser(avatarUrl: String): UserAccountDto {
        val currentUserAccount = getCurrentUser()
        currentUserAccount.avatar = avatarUrl
        currentUserAccount.changed = timeService.now()
        return userAccountRepository.save(currentUserAccount).toDto()
    }

    override fun addHeadphonesToUser(userId: Long, headphonesDto: HeadphonesDto): HeadphonesDto {
        val userAccount = findUserEntityById(userId)
        val entityHeadphones = headphonesDto.toEntity()
        entityHeadphones.userAccount = userAccount
        return headphonesService.save(entityHeadphones)
    }

    override fun addHeadphonesToCurrentUser(headphones: HeadphonesDto): HeadphonesDto {
        val entityHeadphones = headphones.toEntity()
        entityHeadphones.userAccount = getCurrentUser()
        return headphonesService.save(entityHeadphones)
    }

    override fun updateCurrentUser(userChangeRequest: UserAccountChangeRequest): UserAccountDto {
        return getCurrentUser().let {
            if (userChangeRequest.isNotEmpty())
                userAccountRepository.save(it.updateFields(changeRequest = userChangeRequest))
            else it
        }.toDto()
    }

    private fun UserAccountChangeRequest.isNotEmpty(): Boolean =
        (!this.name.isNullOrBlank())
            .or(this.avatar != null)
            .or(this.bornYear != null)
            .or(this.gender != null)
            .or(this.description != null)
            .or(this.photo != null)

    private fun UserAccount.updateFields(changeRequest: UserAccountChangeRequest) =
        this.copy(
            fullName = changeRequest.name?.takeIf { it.isNotBlank() } ?: fullName,
            bornYear = changeRequest.bornYear ?: bornYear,
            gender = changeRequest.gender?.toString() ?: gender,
            avatar = changeRequest.avatar ?: avatar,
            photo = changeRequest.photo ?: photo,
            description = changeRequest.description ?: description,
            changed = timeService.now()
        )

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
