package com.epam.brn.service.impl

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountAdditionalInfoRequest
import com.epam.brn.dto.request.UserAccountChangePasswordRequest
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.model.Headphones
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.FirebaseUserService
import com.epam.brn.service.HeadphonesService
import com.epam.brn.service.UserAccountService
import com.google.firebase.auth.UserRecord
import org.apache.commons.lang3.StringUtils.isNotEmpty
import org.apache.logging.log4j.kotlin.logger
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class UserAccountServiceImpl(
    private val firebaseService: FirebaseUserService,
    private val userAccountRepository: UserAccountRepository,
    private val authorityService: AuthorityService,
    private val headphonesService: HeadphonesService
) : UserAccountService {

    private val log = logger()

    override fun findUserByEmail(email: String): UserAccountResponse {
        return userAccountRepository.findUserAccountByEmail(email)
            .map { it.toDto() }
            .orElseThrow { EntityNotFoundException("No user was found for email=$email") }
    }

    override fun findUserByName(name: String): UserAccountResponse {
        return userAccountRepository
            .findUserAccountByName(name)
            .map(UserAccount::toDto)
            .orElseThrow {
                log.warn("User with `$name` is not found")
                UsernameNotFoundException("User: `$name` is not found")
            }
    }

    override fun findUserByUuid(uuid: String): UserAccountResponse? {
        val user = userAccountRepository.findByUserId(uuid)
        return user?.toDto()
    }

    override fun createUser(
        userAccountCreateRequest: UserAccountCreateRequest,
        firebaseUserRecord: UserRecord
    ): UserAccountResponse {
        val existUser = userAccountRepository.findUserAccountByEmail(userAccountCreateRequest.email)
        existUser.ifPresent {
            throw IllegalArgumentException("The user already exists!")
        }
        val userAccount = userAccountCreateRequest.toModel()
        userAccount.userId = firebaseUserRecord?.uid
        userAccount.authoritySet = getDefaultAuthoritySet()
        return userAccountRepository.save(userAccount).toDto()
    }

    override fun createUserWithFirebase(
        userAccountAdditionalInfoRequest: UserAccountAdditionalInfoRequest,
        firebaseUserRecord: UserRecord
    ): UserAccountResponse {
        val existUser = userAccountRepository.findUserAccountByEmail(firebaseUserRecord.email)
        existUser.ifPresent {
            throw IllegalArgumentException("The user already exists!")
        }
        val userAccount = UserAccount(
            email = firebaseUserRecord.email,
            fullName = firebaseUserRecord.displayName,
            bornYear = userAccountAdditionalInfoRequest.bornYear,
            gender = userAccountAdditionalInfoRequest.gender.toString(),
            userId = userAccountAdditionalInfoRequest.uuid
        )
        if (userAccountAdditionalInfoRequest.avatar != null) {
            userAccount.avatar = userAccountAdditionalInfoRequest.avatar
        }
        userAccount.authoritySet = getDefaultAuthoritySet()
        return userAccountRepository.save(userAccount).toDto()
    }

    override fun findUserById(id: Long): UserAccountResponse {
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

    override fun getUserFromTheCurrentSession(): UserAccountResponse = getCurrentUser().toDto()

    override fun getCurrentUser(): UserAccount {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name ?: getNameFromPrincipals(authentication)
        return userAccountRepository.findUserAccountByEmail(email)
            .orElseThrow { EntityNotFoundException("No user was found for email=$email") }
    }

    override fun getUsers(pageable: Pageable): List<UserAccountResponse> =
        userAccountRepository.findAll().map { it.toDto() }

    override fun getUsersWithAnalytics(pageable: Pageable): List<UserWithAnalyticsResponse> {
        val users = userAccountRepository.findAll().map { it.toAnalyticsDto() }
        // todo fill user models with analytics and write tests
        return users
    }

    override fun updateAvatarForCurrentUser(avatarUrl: String): UserAccountResponse {
        val currentUserAccount = getCurrentUser()
        currentUserAccount.avatar = avatarUrl
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

    override fun updateCurrentUser(userChangeRequest: UserAccountChangeRequest): UserAccountResponse {
        return getCurrentUser().let {
            if (userChangeRequest.isNotEmpty())
                userAccountRepository.save(it.updateFields(changeRequest = userChangeRequest))
            else it
        }.toDto()
    }

    override fun changePasswordCurrentUser(userAccountChangePasswordRequest: UserAccountChangePasswordRequest): Boolean {
        val firebaseUser = firebaseService.getUserById(userAccountChangePasswordRequest.uuid)
        var firebaseUserRecord: UserRecord? = null
        if (firebaseUser != null && firebaseUser.providerId == "password") {
            firebaseUserRecord = firebaseService.changePassword(userAccountChangePasswordRequest)
        }
        return firebaseUserRecord != null
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
            description = changeRequest.description ?: description
        )

    private fun getNameFromPrincipals(authentication: Authentication): String {
        val principal = authentication.principal
        if (principal is UserDetails)
            return principal.username
        if (principal is Principal)
            return principal.name

        throw EntityNotFoundException("There is no user in the session")
    }

    private fun getDefaultAuthoritySet(): MutableSet<Authority> {
        var authorityNames = mutableSetOf<String>()
        if (authorityNames.isEmpty())
            authorityNames = mutableSetOf("ROLE_USER")

        return authorityNames
            .filter(::isNotEmpty)
            .mapTo(mutableSetOf()) {
                authorityService.findAuthorityByAuthorityName(it)
            }
    }
}
