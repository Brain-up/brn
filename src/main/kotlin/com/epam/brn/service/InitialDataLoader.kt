package com.epam.brn.service

import com.epam.brn.auth.AuthorityService
import com.epam.brn.model.Authority
import com.epam.brn.model.UserAccount
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Exercise
import com.epam.brn.model.Signal
import com.epam.brn.model.Gender
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.upload.CsvUploadService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
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
    private val exerciseGroupRepository: ExerciseGroupRepository,
    private val seriesRepository: SeriesRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userAccountRepository: UserAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authorityService: AuthorityService,
    private val uploadService: CsvUploadService,
    private val audioFilesGenerationService: AudioFilesGenerationService,
    private val wordsService: WordsService
) {
    private val log = logger()

    @Value("\${init.folder:#{null}}")
    var directoryPath: Path? = null

    @Value("\${withAudioFilesGeneration}")
    var withAudioFilesGeneration: Boolean = false

    companion object {
        fun fileNameForSeries(seriesId: Long) = "${seriesId}_series.csv"

        fun getInputStreamFromSeriesInitFile(seriesId: Long): InputStream {
            val inputStream = Thread.currentThread()
                .contextClassLoader.getResourceAsStream("initFiles/${fileNameForSeries(seriesId)}")

            if (inputStream == null)
                throw IOException("Can not get init file for $seriesId series.")

            return inputStream
        }
    }

    private val sourceFiles = listOf(
        "groups.csv", "series.csv",
        fileNameForSeries(1),
        fileNameForSeries(2),
        fileNameForSeries(3),
        fileNameForSeries(4)
    )

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

        if (isInitRequired())
            init()

        if (withAudioFilesGeneration)
            audioFilesGenerationService.generateAudioFiles()
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

    private fun isInitRequired() = exerciseGroupRepository.count() == 0L

    private fun init() {
        log.debug("Initialization started")
        if (directoryPath != null)
            initDataFromDirectory(directoryPath!!)
        else
            initDataFromClassPath()
        wordsService.fillWordsWithAudioOggFile()

        create1SeriesNonSpeechGroup()
    }

    private fun create1SeriesNonSpeechGroup() {
        val seriesLength = seriesRepository.findByNameLike("Длительность сигналов")[0]
        val seriesFrequency = seriesRepository.findByNameLike("Частота сигналов")[0]
        val exercise1 = Exercise(series = seriesLength, name = "По 2 сигнала разной длительности.", level = 1,
            exerciseType = ExerciseType.TWO_DIFFERENT_LENGTH_SIGNAL.name)
        val signalOne1 = Signal(frequency = 1000, length = 60, exercise = exercise1)
        val signalTwo1 = Signal(frequency = 1000, length = 120, exercise = exercise1)
        exercise1.addSignals(listOf(signalOne1, signalTwo1))
        val exercise2 = Exercise(series = seriesLength, name = "По 2 сигнала разной длительности.", level = 2,
            exerciseType = ExerciseType.TWO_DIFFERENT_LENGTH_SIGNAL.name)
        val signalOne2 = Signal(frequency = 1000, length = 40, exercise = exercise2)
        val signalTwo2 = Signal(frequency = 1000, length = 100, exercise = exercise2)
        exercise2.addSignals(listOf(signalOne2, signalTwo2))

        val exercise3 = Exercise(series = seriesFrequency, name = "По 2 сигнала разной частоты.", level = 1,
            exerciseType = ExerciseType.THREE_DIFFERENT_FREQUENCY_SIGNAL.name)
        val signalOne3 = Signal(frequency = 500, length = 120, exercise = exercise3)
        val signalTwo3 = Signal(frequency = 1500, length = 120, exercise = exercise3)
        exercise3.addSignals(listOf(signalOne3, signalTwo3))
        val exercise4 = Exercise(series = seriesFrequency, name = "По 2 сигнала разной частоты.", level = 2,
            exerciseType = ExerciseType.THREE_DIFFERENT_FREQUENCY_SIGNAL.name)
        val signalOne4 = Signal(frequency = 1000, length = 120, exercise = exercise4)
        val signalTwo4 = Signal(frequency = 2000, length = 120, exercise = exercise4)
        exercise4.addSignals(listOf(signalOne4, signalTwo4))

        val exercise5 = Exercise(series = seriesLength, name = "По 3 сигнала разной длительности.", level = 1,
            exerciseType = ExerciseType.THREE_DIFFERENT_LENGTH_SIGNAL.name)
        val signalOne5 = Signal(frequency = 1000, length = 40, exercise = exercise5)
        val signalTwo5 = Signal(frequency = 1000, length = 90, exercise = exercise5)
        val signalThree5 = Signal(frequency = 1000, length = 130, exercise = exercise5)
        exercise5.addSignals(listOf(signalOne5, signalTwo5, signalThree5))
        val exercise6 = Exercise(series = seriesLength, name = "По 3 сигнала разной длительности.", level = 2,
            exerciseType = ExerciseType.THREE_DIFFERENT_LENGTH_SIGNAL.name)
        val signalOne6 = Signal(frequency = 1000, length = 20, exercise = exercise6)
        val signalTwo6 = Signal(frequency = 1000, length = 60, exercise = exercise6)
        val signalThree6 = Signal(frequency = 1000, length = 120, exercise = exercise6)
        exercise6.addSignals(listOf(signalOne6, signalTwo6, signalThree6))

        val exercise7 = Exercise(series = seriesFrequency, name = "По 3 сигнала разной частоты.", level = 1,
            exerciseType = ExerciseType.THREE_DIFFERENT_FREQUENCY_SIGNAL.name)
        val signalOne7 = Signal(frequency = 500, length = 120, exercise = exercise7)
        val signalTwo7 = Signal(frequency = 1050, length = 120, exercise = exercise7)
        val signalThree7 = Signal(frequency = 2000, length = 120, exercise = exercise7)
        exercise7.addSignals(listOf(signalOne7, signalTwo7, signalThree7))
        val exercise8 = Exercise(series = seriesFrequency, name = "По 3 сигнала разной частоты.", level = 2,
            exerciseType = ExerciseType.THREE_DIFFERENT_FREQUENCY_SIGNAL.name)
        val signalOne8 = Signal(frequency = 200, length = 120, exercise = exercise8)
        val signalTwo8 = Signal(frequency = 600, length = 120, exercise = exercise8)
        val signalThree8 = Signal(frequency = 2000, length = 120, exercise = exercise8)
        exercise8.addSignals(listOf(signalOne8, signalTwo8, signalThree8))

        exerciseRepository.saveAll(listOf(exercise1, exercise2, exercise3, exercise4,
            exercise4, exercise5, exercise6, exercise7, exercise8))
    }

    private fun initDataFromDirectory(directoryToScan: Path) {
        log.debug("Loading data from $directoryToScan.")
        if (!Files.exists(directoryToScan) || !Files.isDirectory(directoryPath))
            throw IllegalArgumentException("$directoryToScan with initial data does not exist")
        sourceFiles.forEach {
            loadFromInputStream(
                Files.newInputStream(directoryToScan.resolve(it))
            )
        }
    }

    private fun initDataFromClassPath() {
        log.debug("Loading data from classpath 'initFiles' directory.")
        sourceFiles.forEach {
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
}
