package com.epam.brn.service

import com.epam.brn.constant.BrnRoles.AUTH_ROLE_ADMIN
import com.epam.brn.constant.BrnRoles.AUTH_ROLE_USER
import com.epam.brn.csv.CsvUploadService
import com.epam.brn.model.Authority
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.UserAccountRepository
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.core.io.ResourceLoader
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

// TODO: this class smells like GOD-object anti-pattern, it should be split

/**
 * This class is responsible for
 * loading seed data on startup.
 */
@Service
@Profile("dev", "prod")
class InitialDataLoader(
    private val resourceLoader: ResourceLoader,
    private val exerciseGroupRepository: ExerciseGroupRepository,
    private val userAccountRepository: UserAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authorityService: AuthorityService,
    uploadService: CsvUploadService
) {
    private val log = logger()

    @Value("\${init.folder:#{null}}")
    var directoryPath: Path? = null

    companion object {
        fun fileNameForSeries(seriesId: Long) = "${seriesId}_series.csv"

        fun getInputStreamFromSeriesInitFile(seriesId: Long): InputStream {
            val inputStream = Thread.currentThread()
                .contextClassLoader.getResourceAsStream("initFiles/${fileNameForSeries(seriesId)}")

            if (inputStream == null) {
                throw IOException("Can not get init file for $seriesId series.")
            }

            return inputStream
        }
    }

    private val sourceFileLoaders = mapOf<String, (it: InputStream) -> Any>(
        "groups.csv" to uploadService::loadGroups,
        "series.csv" to uploadService::loadSeries,
        "exercises.csv" to uploadService::loadExercises,
        fileNameForSeries(1) to uploadService::loadTasksFor1Series,
        fileNameForSeries(2) to uploadService::loadTasksFor2Series,
        fileNameForSeries(3) to uploadService::loadExercisesFor3Series
    )

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        val adminAuthority = authorityService.save(Authority(authorityName = AUTH_ROLE_ADMIN))
        val userAuthority = authorityService.save(Authority(authorityName = AUTH_ROLE_USER))
        val admin = addAdminUser(adminAuthority)
        val listOfUsers = addDefaultUsers(userAuthority)
        listOfUsers.add(admin)

        userAccountRepository.saveAll(listOfUsers)

        if (isInitRequired()) {
            init()
        }
    }

    private fun addAdminUser(adminAuthority: Authority): UserAccount {
        val password = passwordEncoder.encode("admin")
        val userAccount =
            UserAccount(
                firstName = "admin",
                lastName = "admin",
                password = password,
                email = "admin@admin.com",
                active = true
            )
        userAccount.authoritySet.addAll(setOf(adminAuthority))
        return userAccount
    }

    private fun addDefaultUsers(userAuthority: Authority): MutableList<UserAccount> {
        val password = passwordEncoder.encode("password")
        val firstUser = UserAccount(
            firstName = "firstName",
            lastName = "lastName",
            email = "default@default.ru",
            active = true,
            password = password
        )
        val secondUser = UserAccount(
            firstName = "firstName2",
            lastName = "lastName2",
            email = "default2@default.ru",
            active = true,
            password = password
        )
        firstUser.authoritySet.addAll(setOf(userAuthority))
        secondUser.authoritySet.addAll(setOf(userAuthority))
        return mutableListOf(firstUser, secondUser)
    }

    private fun isInitRequired() = exerciseGroupRepository.count() == 0L

    private fun init() {
        log.debug("Initialization started")

        if (directoryPath != null) {
            initDataFromDirectory(directoryPath!!)
        } else {
            initDataFromClassPath()
        }
    }

    private fun initDataFromDirectory(directoryToScan: Path) {
        log.debug("Loading data from $directoryToScan.")

        if (!Files.exists(directoryToScan) || !Files.isDirectory(directoryPath))
            throw IllegalArgumentException("$directoryToScan with initial data does not exist")

        sourceFileLoaders.forEach {
            loadFromInputStream(
                it.value,
                Files.newInputStream(directoryToScan.resolve(it.key))
            )
        }
    }

    private fun initDataFromClassPath() {
        log.debug("Loading data from classpath 'initFiles' directory.")

        sourceFileLoaders.forEach {
            loadFromInputStream(
                it.value,
                resourceLoader.getResource("classpath:initFiles/${it.key}").inputStream
            )
        }
    }

    private fun loadFromInputStream(
        load: (inputStream: InputStream) -> Any,
        inputStream: InputStream
    ) {
        try {
            load(inputStream)
        } finally {
            closeSilently(inputStream)
        }
    }

    private fun closeSilently(inputStream: InputStream) {
        try {
            inputStream.close()
        } catch (e: Exception) {
            log.error(e)
        }
    }
}
