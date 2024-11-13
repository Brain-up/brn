package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.dto.azure.tts.AzureRates
import com.epam.brn.dto.statistics.DayStudyStatistics
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.BrnRole
import com.epam.brn.enums.Voice
import com.epam.brn.enums.ExerciseType
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.model.projection.UserStatisticView
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.impl.UserAnalyticsServiceImpl
import com.epam.brn.service.statistics.UserPeriodStatisticsService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
@DisplayName("UserAnalyticsService test")
internal class UserAnalyticsServiceTest {

    @InjectMockKs
    lateinit var userAnalyticsService: UserAnalyticsServiceImpl

    @MockK
    lateinit var userAccountRepository: UserAccountRepository

    @MockK
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @MockK
    lateinit var exerciseRepository: ExerciseRepository

    @MockK
    lateinit var userDayStatisticService: UserPeriodStatisticsService<DayStudyStatistics>

    @MockK
    lateinit var timeService: TimeService

    @MockK
    lateinit var textToSpeechService: TextToSpeechService

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    lateinit var exerciseService: ExerciseService

    @MockK
    lateinit var pageable: Pageable

    @MockK(relaxed = true)
    lateinit var doctorAccount: UserAccount

    @MockK(relaxed = true)
    lateinit var dayStudyStatistics: DayStudyStatistics

    @MockK
    lateinit var userStatisticView: UserStatisticView

    @Test
    fun `should return all users with analytics`() {

        val usersList = listOf(doctorAccount, doctorAccount)
        val dayStatisticList = listOf(dayStudyStatistics, dayStudyStatistics)
        every { userStatisticView.firstStudy } returns LocalDateTime.now()
        every { userStatisticView.lastStudy } returns LocalDateTime.now()
        every { userStatisticView.spentTime } returns 10000L
        every { userStatisticView.doneExercises } returns 1

        every { userAccountRepository.findUsersAccountsByRole(BrnRole.ADMIN) } returns usersList
        every { userDayStatisticService.getStatisticsForPeriod(any(), any(), any()) } returns dayStatisticList
        every { timeService.now() } returns LocalDateTime.now()
        every { studyHistoryRepository.getStatisticsByUserAccountId(any()) } returns userStatisticView

        val userAnalyticsDtos = userAnalyticsService.getUsersWithAnalytics(pageable, BrnRole.ADMIN)

        userAnalyticsDtos.size shouldBe 2
    }

    @Test
    fun `should not return user with analytics`() {

        val usersList = listOf(doctorAccount)
        val dayStatisticList = emptyList<DayStudyStatistics>()
        every { userStatisticView.firstStudy } returns LocalDateTime.now()
        every { userStatisticView.lastStudy } returns LocalDateTime.now()
        every { userStatisticView.spentTime } returns 10000L
        every { userStatisticView.doneExercises } returns 1

        every { userAccountRepository.findUsersAccountsByRole(BrnRole.ADMIN) } returns usersList
        every { userDayStatisticService.getStatisticsForPeriod(any(), any(), any()) } returns dayStatisticList
        every { timeService.now() } returns LocalDateTime.now()
        every { studyHistoryRepository.getStatisticsByUserAccountId(any()) } returns userStatisticView

        val userAnalyticsDtos = userAnalyticsService.getUsersWithAnalytics(pageable, BrnRole.ADMIN)

        userAnalyticsDtos.size shouldBe 1
        userAnalyticsDtos[0].lastWeek.size shouldBe 0
    }

    private val currentUserId = 1L
    private val exerciseId = 11L

    @Test
    fun `should prepareAudioFileMetaData with adding comma and slow speed for words with good stat WORDS`() {
        // GIVEN
        val studyHistory = mockk<StudyHistory>()
        every { userAccountService.getCurrentUserId() } returns currentUserId
        every {
            studyHistoryRepository.findLastByUserAccountIdAndExerciseId(currentUserId, exerciseId)
        } returns studyHistory
        every { exerciseService.isDoneWell(studyHistory) } returns true
        every { exerciseRepository.findTypeByExerciseId(exerciseId) } returns ExerciseType.SINGLE_SIMPLE_WORDS.name
        val audioFileMetaData =
            AudioFileMetaData("мама папа", BrnLocale.RU.locale, Voice.FILIPP.name, "1", AzureRates.DEFAULT)
        // WHEN
        val metaDataResult = userAnalyticsService.prepareAudioFileMetaData(exerciseId, audioFileMetaData)

        // THEN
        metaDataResult.speedFloat shouldBe "0.8"
        metaDataResult.speedCode shouldBe AzureRates.SLOW
        metaDataResult.text shouldBe "мама, папа"
    }

    @Test
    fun `should prepareAudioFileMetaData without adding comma and slow speed for words with good stat PHRASES`() {
        // GIVEN
        val studyHistory = mockk<StudyHistory>()
        every { userAccountService.getCurrentUserId() } returns currentUserId
        every {
            studyHistoryRepository.findLastByUserAccountIdAndExerciseId(currentUserId, exerciseId)
        } returns studyHistory
        every { exerciseService.isDoneWell(studyHistory) } returns true
        every { exerciseRepository.findTypeByExerciseId(exerciseId) } returns ExerciseType.PHRASES.name
        val audioFileMetaData =
            AudioFileMetaData("мама папа", BrnLocale.RU.locale, Voice.FILIPP.name, "1", AzureRates.DEFAULT)
        // WHEN
        val metaDataResult = userAnalyticsService.prepareAudioFileMetaData(exerciseId, audioFileMetaData)

        // THEN
        metaDataResult.speedFloat shouldBe "0.8"
        metaDataResult.speedCode shouldBe AzureRates.SLOW
        metaDataResult.text shouldBe "мама папа"
    }

    @Test
    fun `should prepareAudioFileMetaData with adding comma and slowest speed for words with bad stat WORDS`() {
        // GIVEN
        val studyHistory = mockk<StudyHistory>()
        every { userAccountService.getCurrentUserId() } returns currentUserId
        every {
            studyHistoryRepository.findLastByUserAccountIdAndExerciseId(currentUserId, exerciseId)
        } returns studyHistory
        every { exerciseService.isDoneWell(studyHistory) } returns false
        every { exerciseRepository.findTypeByExerciseId(exerciseId) } returns ExerciseType.SINGLE_SIMPLE_WORDS.name
        val audioFileMetaData =
            AudioFileMetaData("мама папа", BrnLocale.RU.locale, Voice.FILIPP.name, "1", AzureRates.DEFAULT)

        // WHEN
        val metaDataResult = userAnalyticsService.prepareAudioFileMetaData(exerciseId, audioFileMetaData)

        // THEN
        metaDataResult.speedFloat shouldBe "0.65"
        metaDataResult.speedCode shouldBe AzureRates.X_SLOW
        metaDataResult.text shouldBe "мама, папа"
    }

    @Test
    fun `should prepareAudioFileMetaData without adding comma and slowest speed for words with bad stat PHRASES`() {
        // GIVEN
        val studyHistory = mockk<StudyHistory>()
        every { userAccountService.getCurrentUserId() } returns currentUserId
        every {
            studyHistoryRepository.findLastByUserAccountIdAndExerciseId(currentUserId, exerciseId)
        } returns studyHistory
        every { exerciseService.isDoneWell(studyHistory) } returns false
        every { exerciseRepository.findTypeByExerciseId(exerciseId) } returns ExerciseType.PHRASES.name
        val audioFileMetaData =
            AudioFileMetaData("мама папа", BrnLocale.RU.locale, Voice.FILIPP.name, "1", AzureRates.DEFAULT)

        // WHEN
        val metaDataResult = userAnalyticsService.prepareAudioFileMetaData(exerciseId, audioFileMetaData)

        // THEN
        metaDataResult.speedFloat shouldBe "0.65"
        metaDataResult.speedCode shouldBe AzureRates.X_SLOW
        metaDataResult.text shouldBe "мама папа"
    }

    @Test
    fun `should prepareAudioFileMetaData default speed correctly for one word and good statistics`() {
        // GIVEN
        val studyHistory = mockk<StudyHistory>()
        every { userAccountService.getCurrentUserId() } returns currentUserId
        every {
            studyHistoryRepository.findLastByUserAccountIdAndExerciseId(currentUserId, exerciseId)
        } returns studyHistory
        every { exerciseService.isDoneWell(studyHistory) } returns true
        every { exerciseRepository.findTypeByExerciseId(exerciseId) } returns ExerciseType.SINGLE_SIMPLE_WORDS.name
        val audioFileMetaData = AudioFileMetaData("мама", BrnLocale.RU.locale, Voice.FILIPP.name, "1", AzureRates.DEFAULT)

        // WHEN
        val metaDataResult = userAnalyticsService.prepareAudioFileMetaData(exerciseId, audioFileMetaData)

        // THEN
        metaDataResult.speedFloat shouldBe "1"
        metaDataResult.speedCode shouldBe AzureRates.DEFAULT
        metaDataResult.text shouldBe "мама"
    }

    @Test
    fun `should prepareAudioFileMetaData slow correctly for single word and bad statistics`() {
        // GIVEN
        val studyHistory = mockk<StudyHistory>()
        every { userAccountService.getCurrentUserId() } returns currentUserId
        every {
            studyHistoryRepository.findLastByUserAccountIdAndExerciseId(currentUserId, exerciseId)
        } returns studyHistory
        every { exerciseService.isDoneWell(studyHistory) } returns false
        every { exerciseRepository.findTypeByExerciseId(exerciseId) } returns ExerciseType.SINGLE_SIMPLE_WORDS.name
        val audioFileMetaData = AudioFileMetaData("text", BrnLocale.RU.locale, Voice.FILIPP.name, "1", AzureRates.DEFAULT)

        // WHEN
        val metaDataResult = userAnalyticsService.prepareAudioFileMetaData(exerciseId, audioFileMetaData)

        // THEN
        metaDataResult.speedFloat shouldBe "0.8"
        metaDataResult.speedCode shouldBe AzureRates.SLOW
    }
}
