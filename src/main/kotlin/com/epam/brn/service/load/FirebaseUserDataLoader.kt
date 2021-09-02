package com.epam.brn.service.load

import com.epam.brn.repo.UserAccountRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ImportUserRecord
import com.google.firebase.auth.UserImportOptions
import com.google.firebase.auth.hash.Bcrypt
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.UUID

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

        while (true) {
            val pageRequest = PageRequest.of(0, batchCount)
            val foundedUsers = userAccountRepository.findAllByUserIdIsNull(pageRequest)
            if (foundedUsers.totalElements <= 0) {
                break
            }

            foundedUsers.content
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
                foundedUsers.content[it.index].userId = null
            }
            userAccountRepository.saveAll(foundedUsers)
        }
    }
}
