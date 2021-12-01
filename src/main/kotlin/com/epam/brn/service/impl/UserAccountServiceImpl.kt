package com.epam.brn.service.impl

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.enums.Role
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.model.Headphones
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
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
        firebaseUserRecord: UserRecord
    ): UserAccountResponse {
        val existUser = userAccountRepository.findUserAccountByEmail(firebaseUserRecord.email)
        existUser.ifPresent {
            throw IllegalArgumentException("The user already exists!")
        }
        val userAccount = UserAccount(
            fullName = firebaseUserRecord.displayName,
            email = firebaseUserRecord.email,
            userId = firebaseUserRecord.uid
        )
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

    override fun getAllHeadphonesForCurrentUser() = getCurrentUser()
        .headphones.filter { it.active }.map(Headphones::toDto).toSet()

    override fun getUserFromTheCurrentSession(): UserAccountResponse = getCurrentUser().toDto()

    override fun getCurrentUser(): UserAccount {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication.name ?: getNameFromPrincipals(authentication)
        return userAccountRepository.findUserAccountByEmail(email)
            .orElseThrow { EntityNotFoundException("No user was found for email=$email") }
    }

    override fun getUsers(pageable: Pageable, role: String): List<UserAccountResponse> =
        userAccountRepository.findUsersAccountsByRole(role).map { it.toDto() }

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

    override fun deleteHeadphonesForCurrentUser(headphonesId: Long) {
        val currentUserAccount = getCurrentUser()
        val headphones = currentUserAccount.headphones.firstOrNull { it.id == headphonesId }
            ?: throw EntityNotFoundException("Can not delete headphones. No headphones was found by Id=$headphonesId")
        headphones.active = false
        headphonesService.save(headphones)
    }

    override fun updateCurrentUser(userChangeRequest: UserAccountChangeRequest): UserAccountResponse {
        return getCurrentUser().let {
            if (userChangeRequest.isNotEmpty())
                userAccountRepository.save(it.updateFields(changeRequest = userChangeRequest))
            else it
        }.toDto()
    }

    override fun updateDoctorForPatient(userId: Long, doctorId: Long): UserAccount =
        userAccountRepository.save(findUserEntityById(userId).apply { doctor = findUserEntityById(doctorId) })

    override fun removeDoctorFromPatient(userId: Long): UserAccount =
        userAccountRepository.save(findUserEntityById(userId).apply { doctor = null })

    override fun getPatientsForDoctor(doctorId: Long): List<UserAccountResponse> =
        userAccountRepository.findUserAccountsByDoctor(findUserEntityById(doctorId)).map { it.toDto() }

    private fun UserAccountChangeRequest.isNotEmpty(): Boolean =
        (!this.name.isNullOrBlank())
            .or(this.avatar != null)
            .or(this.bornYear != null)
            .or(this.gender != null)
            .or(this.description != null)
            .or(this.photo != null)

    private fun UserAccount.updateFields(changeRequest: UserAccountChangeRequest): UserAccount {
        this.fullName = changeRequest.name?.takeIf { it.isNotBlank() } ?: fullName
        this.bornYear = changeRequest.bornYear ?: bornYear
        this.gender = changeRequest.gender?.toString() ?: gender
        this.avatar = changeRequest.avatar ?: avatar
        this.photo = changeRequest.photo ?: photo
        this.description = changeRequest.description ?: description
        return this
    }

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
            authorityNames = mutableSetOf(Role.ROLE_USER.name)

        return authorityNames
            .filter(::isNotEmpty)
            .mapTo(mutableSetOf()) {
                authorityService.findAuthorityByAuthorityName(it)
            }
    }
}
