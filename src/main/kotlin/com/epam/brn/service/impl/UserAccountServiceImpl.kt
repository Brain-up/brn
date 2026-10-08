package com.epam.brn.service.impl

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.enums.BrnRole
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Headphones
import com.epam.brn.model.Role
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.HeadphonesService
import com.epam.brn.service.RoleService
import com.epam.brn.service.TimeService
import com.epam.brn.service.UserAccountService
import com.google.firebase.auth.UserRecord
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Principal
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executor

@Service
class UserAccountServiceImpl(
    private val userAccountRepository: UserAccountRepository,
    private val roleService: RoleService,
    private val headphonesService: HeadphonesService,
    private val timeService: TimeService,
    @Qualifier("lastVisitUpdateExecutor")
    private val lastVisitUpdateExecutor: Executor,
) : UserAccountService {
    private val log = logger()

    @Volatile
    private var recentLastVisitUpdatesCache: Cache<String, LocalDateTime>? = null

    @Value("\${autotest.users.deletion.prefix}")
    private lateinit var prefix: String

    @Value("\${brn.user.last-visit.update-min-interval:PT15M}")
    private var lastVisitUpdateMinInterval: Duration = DEFAULT_LAST_VISIT_UPDATE_MIN_INTERVAL

    override fun findUserByEmail(email: String): UserAccountDto = userAccountRepository
        .findUserAccountByEmail(email)
        .map { it.toDto() }
        .orElseThrow { EntityNotFoundException("No user was found for email=$email") }

    override fun findUserDtoByUuid(uuid: String): UserAccountDto? = userAccountRepository.findByUserId(uuid)?.toDto()

    override fun createUser(firebaseUserRecord: UserRecord): UserAccountDto {
        val existUser = userAccountRepository.findUserAccountByEmail(firebaseUserRecord.email)
        existUser.ifPresent {
            throw IllegalArgumentException("The user already exists!")
        }
        val userAccount =
            UserAccount(
                fullName = firebaseUserRecord.displayName,
                email = firebaseUserRecord.email,
                userId = firebaseUserRecord.uid,
            )
        userAccount.roleSet = getDefaultRoleSet()
        return userAccountRepository.save(userAccount).toDto()
    }

    override fun findUserDtoById(id: Long): UserAccountDto = findUserById(id).toDto()

    override fun findUserById(id: Long): UserAccount = userAccountRepository
        .findUserAccountById(id)
        .orElseThrow { EntityNotFoundException("No user was found for id = $id") }

    override fun getAllHeadphonesForUser(userId: Long) = headphonesService.getAllHeadphonesForUser(userId)

    override fun getAllHeadphonesForCurrentUser() = getCurrentUser()
        .headphones
        .filter { it.active }
        .map(Headphones::toDto)
        .toSet()

    override fun getCurrentUser(): UserAccount {
        val email = getCurrentUserEmail()
        return userAccountRepository
            .findUserAccountByEmail(email)
            .orElseThrow { EntityNotFoundException("No user was found for email=$email") }
    }

    override fun markVisitForCurrentUser() {
        val email = getCurrentUserEmail()
        val lastVisit = timeService.now()
        if (!shouldScheduleLastVisitUpdate(email, lastVisit)) return

        try {
            lastVisitUpdateExecutor.execute {
                try {
                    userAccountRepository.updateLastVisitByEmailIfOlderThan(
                        email = email,
                        lastVisit = lastVisit,
                        staleBefore = lastVisit.minus(lastVisitUpdateMinInterval),
                    )
                } catch (e: Exception) {
                    invalidateScheduledLastVisitUpdate(email)
                    log.error("Error while updating lastVisit for email=$email: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            invalidateScheduledLastVisitUpdate(email)
            log.error("Error while scheduling lastVisit update for email=$email: ${e.message}", e)
        }
    }

    override fun getCurrentUserDto(): UserAccountDto = getCurrentUser().toDto()

    override fun getCurrentUserRoles(): Set<String> = getCurrentUserDto().roles.toSet()

    override fun getCurrentUserId(): Long = getCurrentUser().id!!

    override fun getUsers(
        pageable: Pageable,
        role: String,
    ): List<UserAccountDto> = userAccountRepository
        .findUsersAccountsByRole(role)
        .map { it.toDto() }

    override fun updateAvatarForCurrentUser(avatarUrl: String): UserAccountDto {
        val currentUserAccount = getCurrentUser()
        currentUserAccount.avatar = avatarUrl
        return userAccountRepository.save(currentUserAccount).toDto()
    }

    override fun addHeadphonesToUser(
        userId: Long,
        headphonesDto: HeadphonesDto,
    ): HeadphonesDto {
        val userAccount = findUserById(userId)
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
        val headphones =
            currentUserAccount.headphones.firstOrNull { it.id == headphonesId }
                ?: throw EntityNotFoundException("Can not delete headphones. No headphones was found by Id=$headphonesId")
        headphones.active = false
        headphonesService.save(headphones)
    }

    override fun updateCurrentUser(userChangeRequest: UserAccountChangeRequest): UserAccountDto = getCurrentUser()
        .let {
            if (userChangeRequest.isNotEmpty())
                userAccountRepository.save(it.updateFields(changeRequest = userChangeRequest))
            else
                it
        }.toDto()

    override fun updateDoctorForPatient(
        userId: Long,
        doctorId: Long,
    ): UserAccount = userAccountRepository.save(findUserById(userId).apply { doctor = findUserById(doctorId) })

    override fun removeDoctorFromPatient(userId: Long): UserAccount =
        userAccountRepository.save(findUserById(userId).apply { doctor = null })

    override fun getPatientsForDoctor(doctorId: Long): List<UserAccountDto> =
        userAccountRepository.findUserAccountsByDoctor(findUserById(doctorId)).map { it.toDto() }

    private fun UserAccountChangeRequest.isNotEmpty(): Boolean = (!this.name.isNullOrBlank())
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

    private fun getCurrentUserEmail(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.name ?: getNameFromPrincipals(authentication)
    }

    private fun getNameFromPrincipals(authentication: Authentication): String {
        val principal = authentication.principal
        if (principal is UserDetails)
            return principal.username
        if (principal is Principal)
            return principal.name
        throw EntityNotFoundException("There is no user in the session")
    }

    private fun shouldScheduleLastVisitUpdate(
        email: String,
        now: LocalDateTime,
    ): Boolean {
        val recentLastVisitUpdates = recentLastVisitUpdatesCache().asMap()
        val staleBefore = now.minus(lastVisitUpdateMinInterval)
        while (true) {
            val lastScheduledAt = recentLastVisitUpdates[email]
            if (lastScheduledAt != null && !lastScheduledAt.isBefore(staleBefore)) return false
            if (lastScheduledAt == null) {
                if (recentLastVisitUpdates.putIfAbsent(email, now) == null) return true
                continue
            }
            if (recentLastVisitUpdates.replace(email, lastScheduledAt, now)) return true
        }
    }

    private fun recentLastVisitUpdatesCache(): Cache<String, LocalDateTime> = recentLastVisitUpdatesCache
        ?: synchronized(this) {
            recentLastVisitUpdatesCache
                ?: Caffeine
                    .newBuilder()
                    .expireAfterWrite(lastVisitUpdateMinInterval)
                    .maximumSize(10_000)
                    .build<String, LocalDateTime>()
                    .also { recentLastVisitUpdatesCache = it }
        }

    private fun invalidateScheduledLastVisitUpdate(email: String) {
        recentLastVisitUpdatesCache?.invalidate(email)
    }

    private fun getDefaultRoleSet(): MutableSet<Role> = setOf(BrnRole.USER).mapTo(mutableSetOf()) { roleService.findByName(it) }

    override fun deleteAutoTestUsers(): Long = userAccountRepository.deleteUserAccountsByEmailStartsWith(prefix)
    override fun deleteAutoTestUserByEmail(email: String): Long = if (email.startsWith(prefix))
        userAccountRepository.deleteUserAccountByEmailIs(email)
    else
        throw IllegalArgumentException("email = [$email] must start with prefix = [$prefix]")

    companion object {
        private val DEFAULT_LAST_VISIT_UPDATE_MIN_INTERVAL: Duration = Duration.ofMinutes(15)
    }
}
