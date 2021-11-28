package com.epam.brn.service.load

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
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.stream.Collectors

@Service
class FirebaseUserDataLoader(
    val firebaseAuth: FirebaseAuth,
    val userAccountRepository: UserAccountRepository
) {

    private val log = logger()

    @Value("\${firebase.import.batch-count}")
    private var batchCount: Int = 100

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        val users = ArrayList<ImportUserRecord>()
        val options = UserImportOptions.withHash(Bcrypt.getInstance())

        if (batchCount > 100) {
            batchCount = 100
        }
        while (true) {
            val pageRequest = PageRequest.of(0, batchCount)
            val foundedUsers = userAccountRepository.findAllByUserIdIsNull(pageRequest)
            if (foundedUsers.totalElements <= 0) {
                break
            }

            val foundedUsersContent = foundedUsers.content
            val userEmails = foundedUsersContent.stream()
                .map { EmailIdentifier(it.email) }
                .collect(Collectors.toList())

            val foundedFirebaseUsers = firebaseAuth.getUsers(userEmails as Collection<UserIdentifier>?)
            val map = foundedFirebaseUsers.users.associateBy { it.email }

            foundedUsersContent
                .filter {
                    !map.containsKey(it.email)
                }
                .forEach {
                    it.userId = UUID.randomUUID().toString()
                    users.add(
                        ImportUserRecord.builder()
                            .setEmail(it.email)
                            .setDisplayName(it.fullName)
                            .setEmailVerified(true)
                            .setPhotoUrl(it.photo)
                            .setUid(it.userId)
                            .setPasswordHash(it.password?.encodeToByteArray())
                            .build()

                    )
                }

            val importUsers = firebaseAuth.importUsers(users, options)
            importUsers.errors.stream().forEach {
                log.error("Import user to firebase error: ${it.reason}")
                foundedUsersContent[it.index].userId = null
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
}
