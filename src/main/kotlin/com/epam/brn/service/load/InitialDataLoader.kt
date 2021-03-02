package com.epam.brn.service.load

import com.epam.brn.auth.AuthorityService
import com.epam.brn.model.Authority
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Gender
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.AudioFilesGenerationService
import com.epam.brn.upload.CsvUploadService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * This class is responsible for
 * loading seed data on startup.
 */
@Service
@Profile("dev", "prod")
class InitialDataLoader(
    private val resourceLoader: ResourceLoader,
    private val userAccountRepository: UserAccountRepository,
    private val audiometryLoader: AudiometryLoader,
    private val passwordEncoder: PasswordEncoder,
    private val authorityService: AuthorityService,
    private val uploadService: CsvUploadService,
    private val audioFilesGenerationService: AudioFilesGenerationService
) {

    @Autowired
    private lateinit var environment: Environment

    private val log = logger()

    @Value("\${init.folder:#{null}}")
    var directoryPath: Path? = null

    @Value("\${withAudioFilesGeneration}")
    var withAudioFilesGeneration: Boolean = false

    companion object {
        private val mapSeriesTypeInitFile = mapOf(
            ExerciseType.SINGLE_SIMPLE_WORDS.name to SINGLE_SIMPLE_WORDS_FILE_NAME,
            ExerciseType.PHRASES.name to PHRASES_FILE_NAME,
            ExerciseType.WORDS_SEQUENCES.name to WORDS_SEQUENCES_FILE_NAME,
            ExerciseType.SENTENCE.name to SENTENCES_FILE_NAME,
            ExerciseType.DURATION_SIGNALS.name to SIGNALS_FILE_NAME,
            ExerciseType.FREQUENCY_SIGNALS.name to SIGNALS_FILE_NAME,
        )

        fun getInputStreamFromSeriesInitFile(seriesType: String): InputStream {
            val fileName = mapSeriesTypeInitFile[seriesType]
            val inputStream = Thread.currentThread()
                .contextClassLoader.getResourceAsStream("initFiles/$fileName.csv")

            if (inputStream == null)
                throw IOException("Can not get init file for $seriesType series.")

            return inputStream
        }
    }

    fun getSourceFiles(): List<String> {
        var profile: String = environment.activeProfiles[0].toLowerCase()
        val suffix = if (profile == "dev") "-dev" else ""
        return listOf(
            "groups.csv",
            "series.csv",
            "subgroups.csv",
            "$SINGLE_SIMPLE_WORDS_FILE_NAME$suffix.csv",
            "$WORDS_SEQUENCES_FILE_NAME$suffix.csv",
            "$PHRASES_FILE_NAME$suffix.csv",
            "signal_exercises.csv",
            "$SENTENCES_FILE_NAME$suffix.csv",
            "lopotko.csv"
        )
    }

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (authorityService.findAll().isEmpty()) {
            val adminAuthority = authorityService.save(Authority(authorityName = "ROLE_ADMIN"))
            val userAuthority = authorityService.save(Authority(authorityName = "ROLE_USER"))
            val admin = addAdminUser(adminAuthority)
            val listOfUsers = mutableListOf(admin)
            listOfUsers.addAll(addDefaultUsers(userAuthority))
            userAccountRepository.saveAll(listOfUsers)
        }

        audiometryLoader.loadInitialAudiometricsWithTasks()

        initExercisesFromFiles()

        if (withAudioFilesGeneration)
            audioFilesGenerationService.generateAudioFiles()
    }

    private fun initExercisesFromFiles() {
        log.debug("Initialization started")
        if (directoryPath != null)
            initDataFromDirectory(directoryPath!!)
        else
            initDataFromClassPath()
    }

    private fun initDataFromDirectory(directoryToScan: Path) {
        log.debug("Loading data from $directoryToScan.")
        if (!Files.exists(directoryToScan) || !Files.isDirectory(directoryPath))
            throw IllegalArgumentException("$directoryToScan with initial data does not exist")
        getSourceFiles().forEach {
            loadFromInputStream(
                Files.newInputStream(directoryToScan.resolve(it))
            )
        }
    }

    private fun initDataFromClassPath() {
        log.debug("Loading data from classpath 'initFiles' directory.")
        getSourceFiles().forEach {
            loadFromInputStream(
                resourceLoader.getResource("classpath:initFiles/$it").inputStream
            )
        }
    }

    private fun loadFromInputStream(inputStream: InputStream) {
        try {
            uploadService.load(inputStream)
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

    private fun addAdminUser(adminAuthority: Authority): UserAccount {
        val password = passwordEncoder.encode("admin")
        val userAccount =
            UserAccount(
                fullName = "admin",
                password = password,
                email = "admin@admin.com",
                active = true,
                bornYear = 1999,
                gender = Gender.MALE.toString()
            )
        userAccount.authoritySet.addAll(setOf(adminAuthority))
        return userAccount
    }

    private fun addDefaultUsers(userAuthority: Authority): MutableList<UserAccount> {
        val password = passwordEncoder.encode("password")
        val firstUser = UserAccount(
            fullName = "Name1",
            email = "default@default.ru",
            active = true,
            bornYear = 1999,
            gender = Gender.MALE.toString(),
            password = password
        )
        val secondUser = UserAccount(
            fullName = "Name2",
            email = "default2@default.ru",
            active = true,
            bornYear = 1999,
            gender = Gender.FEMALE.toString(),
            password = password
        )
        firstUser.authoritySet.addAll(setOf(userAuthority))
        secondUser.authoritySet.addAll(setOf(userAuthority))
        return mutableListOf(firstUser, secondUser)
    }
}

val SINGLE_SIMPLE_WORDS_FILE_NAME = "series_words"
val PHRASES_FILE_NAME = "series_phrases"
val WORDS_SEQUENCES_FILE_NAME = "series_word_groups"
val SENTENCES_FILE_NAME = "series_sentences"
val SIGNALS_FILE_NAME = "signal_exercises"
