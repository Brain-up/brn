package com.epam.brn.service.load

import com.epam.brn.auth.AuthorityService
import com.epam.brn.enums.AuthorityType
import com.epam.brn.enums.BrnLocale
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Gender
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.AudioFilesGenerationService
import com.epam.brn.service.WordsService
import com.epam.brn.upload.CsvUploadService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
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
    private val audioFilesGenerationService: AudioFilesGenerationService,
    private val wordsService: WordsService,
) {
    private val log = logger()

    @Autowired
    private lateinit var environment: Environment

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
            ExerciseType.FREQUENCY_WORDS.name to SINGLE_FREQUENCY_WORDS_FILE_NAME,
            ExerciseType.DURATION_SIGNALS.name to SIGNALS_FILE_NAME,
            ExerciseType.FREQUENCY_SIGNALS.name to SIGNALS_FILE_NAME,
            ExerciseType.SINGLE_WORDS_KOROLEVA.name to SINGLE_WORDS_KOROLEVA_FILE_NAME,
        )

        fun getInputStreamFromSeriesInitFile(seriesType: String): InputStream {
            val fileName = mapSeriesTypeInitFile[seriesType]
            return Thread.currentThread().contextClassLoader.getResourceAsStream("initFiles/$fileName.csv")
                ?: throw IOException("Can not get init file for $seriesType series.")
        }
    }

    fun getSourceFiles(): List<String> {
        var profile: String = environment.activeProfiles[0].lowercase()
        val devSubFolder = if (profile == "dev") "dev/" else ""
        return listOf(
            "groups_.csv",
            "series_.csv",
            "subgroups_ru.csv",
            "subgroups_en.csv",
            "$devSubFolder$SINGLE_SIMPLE_WORDS_FILE_NAME.csv",
            "$devSubFolder$SINGLE_SIMPLE_WORDS_EN_FILE_NAME.csv",
            "$devSubFolder$SINGLE_FREQUENCY_WORDS_FILE_NAME.csv",
            "$devSubFolder$SINGLE_FREQUENCY_WORDS_EN_FILE_NAME.csv",
            "$devSubFolder$WORDS_SEQUENCES_FILE_NAME.csv",
            "$devSubFolder$PHRASES_FILE_NAME.csv",
            "$SINGLE_WORDS_KOROLEVA_FILE_NAME.csv",
            "signal_exercises_ru.csv",
            "signal_exercises_en.csv",
            "$devSubFolder$SENTENCES_FILE_NAME.csv",
            "$devSubFolder$SENTENCES_EN_FILE_NAME.csv",
            "lopotko_ru.csv"
        )
    }

    @EventListener(ApplicationReadyEvent::class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (authorityService.findAll().isEmpty()) {
            val adminAuthority = authorityService.save(Authority(authorityName = AuthorityType.ROLE_ADMIN.name))
            val userAuthority = authorityService.save(Authority(authorityName = AuthorityType.ROLE_USER.name))
            val specialistAuthority =
                authorityService.save(Authority(authorityName = AuthorityType.ROLE_SPECIALIST.name))
            val admin = addAdminUser(setOf(adminAuthority, userAuthority, specialistAuthority))
            val listOfUsers = mutableListOf(admin)
            listOfUsers.addAll(addDefaultUsers(userAuthority))
            userAccountRepository.saveAll(listOfUsers)
        }
        try {
            authorityService.findAuthorityByAuthorityName(AuthorityType.ROLE_SPECIALIST.name)
        } catch (e: EntityNotFoundException) {
            authorityService.save(Authority(authorityName = AuthorityType.ROLE_SPECIALIST.name))
        }
        addAdminAllAuthorities()
        audiometryLoader.loadInitialAudiometricsWithTasks()
        initExercisesFromFiles()
        wordsService.createTxtFilesWithExerciseWordsMap()

        if (withAudioFilesGeneration)
            audioFilesGenerationService.generateAudioFiles()
    }

    private fun addAdminAllAuthorities() {
        val admin = userAccountRepository.findUserAccountByEmail(ADMIN_EMAIL).get()
        val allAuths = authorityService.findAll()
        admin.authoritySet.addAll(allAuths.minus(admin.authoritySet))
        userAccountRepository.save(admin)
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
                Files.newInputStream(directoryToScan.resolve(it)),
                uploadService.getLocaleFromFileName(it)
            )
        }
    }

    private fun initDataFromClassPath() {
        log.debug("Loading data from classpath 'initFiles' directory.")
        getSourceFiles().forEach {
            loadFromInputStream(
                resourceLoader.getResource("classpath:initFiles/$it").inputStream,
                uploadService.getLocaleFromFileName(it)
            )
        }
    }

    private fun loadFromInputStream(inputStream: InputStream, locale: BrnLocale) {
        try {
            uploadService.load(inputStream, locale)
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

    private fun addAdminUser(adminAuthorities: Set<Authority>): UserAccount {
        val password = passwordEncoder.encode("admin")
        val userAccount =
            UserAccount(
                fullName = "admin",
                email = ADMIN_EMAIL,
                active = true,
                bornYear = 1999,
                gender = Gender.MALE.toString()
            )
        userAccount.password = password
        userAccount.authoritySet.addAll(adminAuthorities)
        return userAccount
    }

    private fun addDefaultUsers(userAuthority: Authority): MutableList<UserAccount> {
        val password = passwordEncoder.encode("password")
        val firstUser = UserAccount(
            fullName = "Name1",
            email = "default@default.ru",
            active = true,
            bornYear = 1999,
            gender = Gender.MALE.toString()
        )
        val secondUser = UserAccount(
            fullName = "Name2",
            email = "default2@default.ru",
            active = true,
            bornYear = 1999,
            gender = Gender.FEMALE.toString()
        )
        firstUser.password = password
        secondUser.password = password
        firstUser.authoritySet.addAll(setOf(userAuthority))
        secondUser.authoritySet.addAll(setOf(userAuthority))
        return mutableListOf(firstUser, secondUser)
    }
}

const val ADMIN_EMAIL = "admin@admin.com"

const val SINGLE_SIMPLE_WORDS_FILE_NAME = "series_words_ru"
const val SINGLE_SIMPLE_WORDS_EN_FILE_NAME = "series_words_en"
const val SINGLE_FREQUENCY_WORDS_FILE_NAME = "series_frequency_words_ru"
const val SINGLE_FREQUENCY_WORDS_EN_FILE_NAME = "series_frequency_words_en"
const val PHRASES_FILE_NAME = "series_phrases_ru"
const val WORDS_SEQUENCES_FILE_NAME = "series_word_groups_ru"
const val SENTENCES_FILE_NAME = "series_sentences_ru"
const val SENTENCES_EN_FILE_NAME = "series_sentences_en"
const val SIGNALS_FILE_NAME = "signal_exercises_"
const val SINGLE_WORDS_KOROLEVA_FILE_NAME = "series_words_koroleva_ru"
