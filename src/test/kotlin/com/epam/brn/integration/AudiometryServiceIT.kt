package com.epam.brn.integration

import com.epam.brn.enums.AudiometryType
import com.epam.brn.enums.FrequencyZone
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.enums.Locale
import com.epam.brn.model.Audiometry
import com.epam.brn.model.AudiometryHistory
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.Gender
import com.epam.brn.model.Headphones
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AudiometryHistoryRepository
import com.epam.brn.repo.AudiometryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import com.epam.brn.repo.HeadphonesRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.AudiometryService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
internal class AudiometryServiceIT {

    @Autowired
    lateinit var audiometryService: AudiometryService

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var audiometryRepository: AudiometryRepository

    @Autowired
    lateinit var audiometryTaskRepository: AudiometryTaskRepository

    @Autowired
    lateinit var audiometryHistoryRepository: AudiometryHistoryRepository

    @Autowired
    lateinit var headphonesRepository: HeadphonesRepository

    @AfterEach
    fun deleteAfterTest() {
        audiometryHistoryRepository.deleteAll()
        audiometryTaskRepository.deleteAll()
        audiometryRepository.deleteAll()
        userAccountRepository.deleteAll()
        headphonesRepository.deleteAll()
    }

    @Test
    fun `should find Second Audiometry Tasks for User`() {
        // GIVEN
        val user = insertUser()
        val audiometry = insertSpeechAudiometry()
        val tasks = insertSpeechAudiometryTasks(audiometry)
        val headphones = insetHeadphones()
        insertHistory(user, headphones, tasks[0], LocalDateTime.now().minusDays(1))
        insertHistory(user, headphones, tasks[1])
        insertHistory(user, headphones, tasks[3])
        insertHistory(user, headphones, tasks[5], LocalDateTime.now().minusDays(1))
        insertHistory(user, headphones, tasks[6])
        val audiometryWithTasks = audiometryRepository.findByIdWithTasks(audiometry.id!!).get()
        // WHEN
        val resultTasks = audiometryService.findSecondSpeechAudiometryTasks(user, audiometryWithTasks)
        // THEN
        assertEquals(4, resultTasks.size)
        assertTrue(resultTasks.containsAll(listOf(tasks[2], tasks[4], tasks[5], tasks[7])))
    }

    private fun insetHeadphones() = headphonesRepository.save(Headphones(name = "first", type = HeadphonesType.OVER_EAR_BLUETOOTH))

    private fun insertUser(): UserAccount = userAccountRepository.save(
        UserAccount(
            fullName = "testUserFirstName",
            gender = Gender.MALE.toString(),
            bornYear = 2000,
            email = "test@test.test",
            active = true
        )
    )

    private fun insertSpeechAudiometry(): Audiometry =
        audiometryRepository.save(
            Audiometry(
                locale = Locale.EN.locale,
                name = "Speech diagnostic",
                description = "Speech diagnostic with Lopotko words sequences",
                audiometryType = AudiometryType.SPEECH.name
            )
        )

    private fun insertSpeechAudiometryTasks(audiometry: Audiometry): List<AudiometryTask> {
        val task1Low = AudiometryTask(
            audiometryGroup = "А",
            frequencyZone = FrequencyZone.LOW.name,
            audiometry = audiometry,
        )
        val task2Low = AudiometryTask(
            audiometryGroup = "Б",
            frequencyZone = FrequencyZone.LOW.name,
            audiometry = audiometry,
        )
        val task3Low = AudiometryTask(
            audiometryGroup = "В",
            frequencyZone = FrequencyZone.LOW.name,
            audiometry = audiometry,
        )
        val task4MediumLow = AudiometryTask(
            audiometryGroup = "А",
            frequencyZone = FrequencyZone.MEDIUM_LOW.name,
            audiometry = audiometry,
        )
        val task5MediumLow = AudiometryTask(
            audiometryGroup = "Б",
            frequencyZone = FrequencyZone.MEDIUM_LOW.name,
            audiometry = audiometry,
        )
        val task6Medium = AudiometryTask(
            audiometryGroup = "А",
            frequencyZone = FrequencyZone.MEDIUM.name,
            audiometry = audiometry,
        )
        val task7Medium = AudiometryTask(
            audiometryGroup = "Б",
            frequencyZone = FrequencyZone.MEDIUM.name,
            audiometry = audiometry,
        )
        val task8MediumHigh = AudiometryTask(
            audiometryGroup = "А",
            frequencyZone = FrequencyZone.MEDIUM_HIGH.name,
            audiometry = audiometry,
        )

        return audiometryTaskRepository.saveAll(
            listOf(
                task1Low,
                task2Low,
                task3Low,
                task4MediumLow,
                task5MediumLow,
                task6Medium,
                task7Medium,
                task8MediumHigh
            )
        )
    }

    private fun insertHistory(
        userAccount: UserAccount,
        headphones: Headphones,
        task: AudiometryTask,
        startTime: LocalDateTime = LocalDateTime.now()
    ) = audiometryHistoryRepository.save(
        AudiometryHistory(
            userAccount = userAccount,
            audiometryTask = task,
            startTime = startTime,
            tasksCount = 10,
            rightAnswers = 5,
            executionSeconds = 50,
            headphones = headphones
        )
    )
}
