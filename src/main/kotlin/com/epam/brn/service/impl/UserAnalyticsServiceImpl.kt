package com.epam.brn.service.impl

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.enums.ExerciseType
import com.epam.brn.enums.Voice
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.ExerciseSuccessCalculator
import com.epam.brn.service.TextToSpeechService
import com.epam.brn.service.TimeService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import com.epam.brn.service.WordsService
import com.epam.brn.service.statistics.impl.UserDayStatisticsService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
class UserAnalyticsServiceImpl(
    private val userAccountRepository: UserAccountRepository,
    private val studyHistoryRepository: StudyHistoryRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userDayStatisticsService: UserDayStatisticsService,
    private val timeService: TimeService,
    private val textToSpeechService: TextToSpeechService,
    private val userAccountService: UserAccountService,
    private val exerciseSuccessCalculator: ExerciseSuccessCalculator,
    private val wordsService: WordsService,
) : UserAnalyticsService {
    private val listTextExercises = listOf(ExerciseType.SENTENCE, ExerciseType.PHRASES)

    override fun getUsersWithAnalytics(
        pageable: Pageable,
        role: String,
    ): List<UserWithAnalyticsResponse> {
        val users = userAccountRepository.findUsersAccountsByRole(role).map { it.toAnalyticsDto() }
        if (users.isEmpty()) return users
        val userIds = users.mapNotNull { it.id }

        val now = timeService.now()
        val firstWeekDay = WeekFields.of(Locale.getDefault()).dayOfWeek()
        val startDay = now.with(firstWeekDay, 1L)
        val from = startDay.with(LocalTime.MIN)
        val to = startDay.plusDays(7L).with(LocalTime.MAX)
        val startOfCurrentMonth = now.withDayOfMonth(1).with(LocalTime.MIN)

        // Fetch analytics for every user in a few aggregate queries instead of 3 queries per user.
        val weekHistoriesByUser =
            studyHistoryRepository
                .getHistoriesForUsers(userIds, from, to)
                .groupBy { it.userAccount.id }
        val studyDaysByUser =
            studyHistoryRepository
                .countStudyDaysForUsers(userIds, startOfCurrentMonth, now)
                .associate { it.userId to it.studyDays }
        val statisticsByUser =
            studyHistoryRepository
                .getStatisticsByUserAccountIds(userIds)
                .associateBy { it.userId }

        users.onEach { user ->
            user.lastWeek = userDayStatisticsService.buildDayStatistics(weekHistoriesByUser[user.id].orEmpty())
            user.studyDaysInCurrentMonth = studyDaysByUser[user.id] ?: 0

            statisticsByUser[user.id]?.let { userStatistic ->
                user.firstDone = userStatistic.firstStudy
                user.lastDone = userStatistic.lastStudy
                user.spentTime = userStatistic.spentTime.toDuration(DurationUnit.SECONDS)
                user.doneExercises = userStatistic.doneExercises
            }
        }
        return users
    }

    override fun prepareAudioStreamForUser(
        exerciseId: Long,
        audioFileMetaData: AudioFileMetaData,
    ): InputStream = textToSpeechService.generateAudioOggStreamWithValidation(
        prepareAudioFileMetaData(exerciseId, audioFileMetaData),
    )

    override fun prepareAudioFileMetaData(
        exerciseId: Long,
        audioFileMetaData: AudioFileMetaData,
    ): AudioFileMetaData {
        val seriesType =
            ExerciseType.valueOf(
                exerciseRepository.findTypeByExerciseId(exerciseId)
                    ?: throw EntityNotFoundException("No exercise found for id=$exerciseId"),
            )
        val text = audioFileMetaData.text
        if (!listTextExercises.contains(seriesType))
            audioFileMetaData.text = text.replace(" ", ", ")
        val currentUser = userAccountService.getCurrentUser()
        // todo use choseVoiceForUser(currentUser) after moving to yandex speechKit v3
        audioFileMetaData.voice = wordsService.getDefaultWomanVoiceForLocale(audioFileMetaData.locale)
        setSpeedForUser(currentUser, exerciseId, audioFileMetaData)
        return audioFileMetaData
    }

    fun setSpeedForUser(
        user: UserAccount,
        exerciseId: Long,
        audioFileMetaData: AudioFileMetaData,
    ) {
        val lastExerciseHistory =
            studyHistoryRepository
                .findLastByUserAccountIdAndExerciseId(user.id!!, exerciseId)
        if (lastExerciseHistory == null)
            audioFileMetaData.setSpeedNormal()
        else if (isDoneBad(lastExerciseHistory))
            audioFileMetaData.setSpeedSlow()
        else if (isDoneWell(lastExerciseHistory))
            audioFileMetaData.setSpeedFaster()
    }

    fun choseVoiceForUser(user: UserAccount): String {
        if (user.bornYear == null)
            return Voice.MARINA.name
        val ages = LocalDate.now().year - user.bornYear!!
        return if (ages < 19)
            Voice.MARINA.name
        else
            Voice.LERA.name
    }

    fun isDoneBad(lastHistory: StudyHistory?): Boolean = lastHistory != null && !exerciseSuccessCalculator.isSuccessful(lastHistory)

    fun isDoneWell(lastHistory: StudyHistory?): Boolean = lastHistory != null && exerciseSuccessCalculator.isSuccessful(lastHistory)

    fun isMultiWords(seriesType: ExerciseType): Boolean =
        seriesType == ExerciseType.PHRASES || seriesType == ExerciseType.SENTENCE || seriesType == ExerciseType.WORDS_SEQUENCES
}
