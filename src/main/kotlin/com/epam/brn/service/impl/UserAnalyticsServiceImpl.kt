package com.epam.brn.service.impl

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.dto.statistics.DayStudyStatistics
import com.epam.brn.dto.statistics.UserExercisingPeriod
import com.epam.brn.enums.ExerciseType
import com.epam.brn.enums.Voice
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.TextToSpeechService
import com.epam.brn.service.TimeService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import com.epam.brn.service.WordsService
import com.epam.brn.service.statistics.progress.status.ProgressStatusManager
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
    private val timeService: TimeService,
    private val textToSpeechService: TextToSpeechService,
    private val userAccountService: UserAccountService,
    private val exerciseService: ExerciseService,
    private val wordsService: WordsService,
    private val progressManager: ProgressStatusManager<List<StudyHistory>>,
) : UserAnalyticsService {
    private val listTextExercises = listOf(ExerciseType.SENTENCE, ExerciseType.PHRASES)

    override fun getUsersWithAnalytics(
        pageable: Pageable,
        role: String,
    ): List<UserWithAnalyticsResponse> {
        val users = userAccountRepository.findUsersAccountsByRole(role, pageable).map { it.toAnalyticsDto() }
        if (users.isEmpty()) return emptyList()

        val userIds = users.mapNotNull { it.id }

        val now = timeService.now()
        val firstWeekDay = WeekFields.of(Locale.getDefault()).dayOfWeek()
        val startDay = now.with(firstWeekDay, 1L)
        val from = startDay.with(LocalTime.MIN)
        val to = startDay.plusDays(7L).with(LocalTime.MAX)
        val startOfCurrentMonth = now.withDayOfMonth(1).with(LocalTime.MIN)

        val weekHistoriesByUserId =
            studyHistoryRepository
                .getHistoriesByUserIds(userIds, from, to)
                .groupBy { it.userAccount.id }

        val monthHistoriesByUserId =
            studyHistoryRepository
                .getHistoriesByUserIds(userIds, startOfCurrentMonth, now)
                .groupBy { it.userAccount.id }

        val statisticsByUserId =
            studyHistoryRepository
                .getStatisticsByUserIds(userIds)
                .associateBy { it.userId }

        users.onEach { user ->
            val weekHistories = weekHistoriesByUserId[user.id] ?: emptyList()
            user.lastWeek = computeDayStatistics(weekHistories)

            val monthHistories = monthHistoriesByUserId[user.id] ?: emptyList()
            user.studyDaysInCurrentMonth = countWorkDaysForMonth(computeDayStatistics(monthHistories))

            val userStatistic = statisticsByUserId[user.id]
            if (userStatistic != null) {
                user.apply {
                    this.firstDone = userStatistic.firstStudy
                    this.lastDone = userStatistic.lastStudy
                    this.spentTime = userStatistic.spentTime.toDuration(DurationUnit.SECONDS)
                    this.doneExercises = userStatistic.doneExercises
                }
            }
        }
        return users
    }

    private fun computeDayStatistics(histories: List<StudyHistory>): List<DayStudyStatistics> {
        val byDate = histories.groupBy { it.startTime.toLocalDate() }
        return byDate.map { (_, dayHistories) ->
            DayStudyStatistics(
                exercisingTimeSeconds = dayHistories.sumOf { it.executionSeconds },
                date = dayHistories.first().startTime,
                progress = progressManager.getStatus(UserExercisingPeriod.DAY, dayHistories),
            )
        }
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

    fun isDoneBad(lastHistory: StudyHistory?): Boolean = lastHistory != null && !exerciseService.isDoneWell(lastHistory)

    fun isDoneWell(lastHistory: StudyHistory?): Boolean = lastHistory != null && exerciseService.isDoneWell(lastHistory)

    fun isMultiWords(seriesType: ExerciseType): Boolean =
        seriesType == ExerciseType.PHRASES || seriesType == ExerciseType.SENTENCE || seriesType == ExerciseType.WORDS_SEQUENCES

    fun countWorkDaysForMonth(dayStudyStatistics: List<DayStudyStatistics>): Int = dayStudyStatistics
        .map { it.date }
        .groupBy { it.dayOfMonth }
        .keys.size
}
