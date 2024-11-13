package com.epam.brn.service.impl

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.dto.statistics.DayStudyStatistics
import com.epam.brn.enums.ExerciseType
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.TextToSpeechService
import com.epam.brn.service.TimeService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import com.epam.brn.service.statistics.UserPeriodStatisticsService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.io.InputStream
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
    private val userDayStatisticService: UserPeriodStatisticsService<DayStudyStatistics>,
    private val timeService: TimeService,
    private val textToSpeechService: TextToSpeechService,
    private val userAccountService: UserAccountService,
    private val exerciseService: ExerciseService,
) : UserAnalyticsService {

    private val listTextExercises = setOf(ExerciseType.SENTENCE, ExerciseType.PHRASES)

    override fun getUsersWithAnalytics(pageable: Pageable, role: String): List<UserWithAnalyticsResponse> {
        val now = timeService.now()
        val (from, to) = calculateDateRange(now)
        val startOfCurrentMonth = now.withDayOfMonth(1).with(LocalTime.MIN)

        val users = userAccountRepository.findUsersAccountsByRole(role)
        val userIds = users.map { it.id }

        val userStatisticsMap = studyHistoryRepository.getStatisticsByUserAccountIds(userIds)
            .associateBy { it.userId }

        return users.map { user ->
            val userId = user.id
            val analytics = user.toAnalyticsDto().apply {
                lastWeek = userDayStatisticService.getStatisticsForPeriod(from, to, userId)
                studyDaysInCurrentMonth = countWorkDaysForMonth(
                    userDayStatisticService.getStatisticsForPeriod(startOfCurrentMonth, now, userId)
                )
                
                userStatisticsMap[userId]?.let { stats ->
                    firstDone = stats.firstStudy
                    lastDone = stats.lastStudy
                    spentTime = stats.spentTime.toDuration(DurationUnit.SECONDS)
                    doneExercises = stats.doneExercises
                }
            }
            analytics
        }
    }

    override fun prepareAudioFileForUser(exerciseId: Long, audioFileMetaData: AudioFileMetaData): InputStream =
        textToSpeechService.generateAudioOggFileWithValidation(prepareAudioFileMetaData(exerciseId, audioFileMetaData))

    override fun prepareAudioFileMetaData(exerciseId: Long, audioFileMetaData: AudioFileMetaData): AudioFileMetaData {
        val currentUserId = userAccountService.getCurrentUserId()
        val seriesType = ExerciseType.valueOf(exerciseRepository.findTypeByExerciseId(exerciseId))
        
        val lastExerciseHistory = if (audioFileMetaData.text.contains(" ") || !listTextExercises.contains(seriesType)) {
            studyHistoryRepository.findLastByUserAccountIdAndExerciseId(currentUserId, exerciseId)
        } else null

        return audioFileMetaData.apply {
            if (!listTextExercises.contains(seriesType)) {
                text = text.replace(" ", ", ")
            }

            when {
                text.contains(" ") && isDoneBad(lastExerciseHistory) -> setSpeedSlowest()
                text.contains(" ") -> setSpeedSlow()
                isDoneBad(lastExerciseHistory) -> setSpeedSlow()
            }
        }
    }

    fun isDoneBad(lastHistory: StudyHistory?): Boolean =
        lastHistory != null && !exerciseService.isDoneWell(lastHistory)

    fun isMultiWords(seriesType: ExerciseType): Boolean =
        seriesType == ExerciseType.PHRASES || seriesType == ExerciseType.SENTENCE || seriesType == ExerciseType.WORDS_SEQUENCES

    fun countWorkDaysForMonth(dayStudyStatistics: List<DayStudyStatistics>): Int =
        dayStudyStatistics
            .map { it.date }
            .groupBy { it.dayOfMonth }
            .keys.size

    private fun calculateDateRange(now: LocalDateTime): Pair<LocalDateTime, LocalDateTime> {
        val firstWeekDay = WeekFields.of(Locale.getDefault()).dayOfWeek()
        val startDay = now.with(firstWeekDay, 1L)
        val from = startDay.with(LocalTime.MIN)
        val to = startDay.plusDays(7L).with(LocalTime.MAX)
        return from to to
    }
}
