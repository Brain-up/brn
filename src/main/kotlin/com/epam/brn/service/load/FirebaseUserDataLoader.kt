package com.epam.brn.service.load

import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.google.firebase.auth.EmailIdentifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ImportUserRecord
import com.google.firebase.auth.UserIdentifier
import com.google.firebase.auth.UserImportOptions
import com.google.firebase.auth.hash.Bcrypt
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import java.util.UUID
import java.util.stream.Collectors

@Profile("!integration-tests")
@Service
class FirebaseUserDataLoader(
    val firebaseAuth: FirebaseAuth,
    val userAccountRepository: UserAccountRepository,
    val passwordEncoder: PasswordEncoder
) {

    private val log = logger()

    @Value("\${firebase.import.batch-count}")
    private var batchCount: Int = 100

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        val options = UserImportOptions.withHash(Bcrypt.getInstance())

        if (batchCount > 100) batchCount = 100

        while (true) {
            val users = ArrayList<ImportUserRecord>()
            val idUsers = ArrayList<Long?>()
            val pageRequest = PageRequest.of(0, batchCount)
            val foundedUsers = userAccountRepository.findAllByUserIdIsNullAndIsFirebaseErrorIsFalse(pageRequest)
            if (foundedUsers.totalElements <= 0) {
                break
            }

            val foundedUsersContent = foundedUsers.content
            val foundedUsersContentMap = foundedUsersContent.associateBy { it.id }
            val userEmails = foundedUsersContent.stream()
                .map { createEmailIdentified(it) }
                .filter { it != null }
                .collect(Collectors.toList())

            val foundedFirebaseUsers = firebaseAuth.getUsers(userEmails as Collection<UserIdentifier>?)
            val map = foundedFirebaseUsers.users.associateBy { it.email }

            foundedUsersContent
                .filter {
                    !map.containsKey(it.email)
                }
                .forEach {
                    it.userId = UUID.randomUUID().toString()

                    val pwd = if (StringUtils.hasText(it.password)) it.password?.encodeToByteArray()
                    else passwordEncoder.encode(UUID.randomUUID().toString()).encodeToByteArray()

                    try {
                        users.add(
                            ImportUserRecord.builder()
                                .setEmail(it.email)
                                .setDisplayName(it.fullName)
                                .setEmailVerified(true)
                                .setPhotoUrl(it.photo)
                                .setUid(it.userId)
                                .setPasswordHash(pwd)
                                .build()
                        )
                        idUsers.add(it.id)
                    } catch (e: Exception) {
                        log.error("Some error occurred while create ImportUserRecord: $e. Skip record with id: ${it.id}")
                    }
                }

            if (users.size > 0) {
                val importUsers = firebaseAuth.importUsers(users, options)
                importUsers.errors.stream().forEach {
                    log.error("Import user to firebase error: ${it.reason}.")
                    log.debug("Index: ${it.index}, idUsers.size: ${idUsers.size}")
                    val userId = idUsers[it.index]
                    val userAccount = foundedUsersContentMap[userId]
                    userAccount?.userId = null
                    userAccount?.isFirebaseError = true
                    log.debug("Email: ${userAccount?.email}")
                }
            }

            foundedUsersContent
                .filter {
                    map[it.email] != null
                }
                .forEach {
                    val uid = map[it.email]?.uid
                    log.debug("Set uuid \"$uid\" from firebase to local user: ${it.id}")
                    it.userId = uid
                }
            userAccountRepository.saveAll(foundedUsersContent)
        }
    }

    private fun createEmailIdentified(it: UserAccount): EmailIdentifier? {
        return try {
            EmailIdentifier(it.email)
        } catch (e: Exception) {
            null
        }
    }
}
