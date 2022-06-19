package com.epam.brn.service.impl

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.TextToSpeechService
import com.epam.brn.service.TimeService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.io.InputStream
import java.time.LocalTime
import java.time.temporal.WeekFields
import java.util.Locale

@Service
class UserAnalyticsServiceImpl(
    private val userAccountRepository: UserAccountRepository,
    private val studyHistoryRepository: StudyHistoryRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val timeService: TimeService,
    private val textToSpeechService: TextToSpeechService,
    private val userAccountService: UserAccountService,
    private val exerciseService: ExerciseService,
) : UserAnalyticsService {

    override fun getUsersWithAnalytics(pageable: Pageable, role: String): List<UserWithAnalyticsResponse> {
        val users = userAccountRepository.findUsersAccountsByRole(role).map { it.toAnalyticsDto() }

        val now = timeService.now()
        val firstWeekDay = WeekFields.of(Locale.getDefault()).dayOfWeek()
        val startDay = now.with(firstWeekDay, 1L)
        val from = startDay.with(LocalTime.MIN)
        val to = startDay.plusDays(7L).with(LocalTime.MAX)
        val startOfLastMonth = now.minusMonths(1).withDayOfMonth(1).with(LocalTime.MIN)
        val endOfLastMonth = now.withDayOfMonth(1).minusDays(1).with(LocalTime.MAX)

        users.onEach {
            it.lastWeek = userDayStatisticService.getStatisticForPeriod(from, to, it.id)
            it.studyDaysInLastMonth = countWorkDaysForMonth(
                userDayStatisticService.getStatisticForPeriod(startOfLastMonth, endOfLastMonth, it.id)
            )
        }

        return users
    }

    override fun prepareAudioFileForUser(exerciseId: Long, audioFileMetaData: AudioFileMetaData): InputStream =
        textToSpeechService.generateAudioOggFileWithValidation(prepareAudioFileMetaData(exerciseId, audioFileMetaData))

    override fun prepareAudioFileMetaData(exerciseId: Long, audioFileMetaData: AudioFileMetaData): AudioFileMetaData {
        val currentUserId = userAccountService.getCurrentUserId()
        val lastExerciseHistory = studyHistoryRepository
            .findLastByUserAccountIdAndExerciseId(currentUserId, exerciseId)
        val seriesType = ExerciseType.valueOf(exerciseRepository.findTypeByExerciseId(exerciseId))
//        when {
//            isMultiWords(seriesType) && isDoneBad(lastExerciseHistory) -> audioFileMetaData.setSpeedSlowest()
//            isMultiWords(seriesType) && !isDoneBad(lastExerciseHistory) -> audioFileMetaData.setSpeedSlow()
//            !isMultiWords(seriesType) && isDoneBad(lastExerciseHistory) -> audioFileMetaData.setSpeedSlow()
//        }
        val text = audioFileMetaData.text
        if (seriesType != ExerciseType.SENTENCE)
            audioFileMetaData.text = text.replace(" ", ", ")

        if (text.contains(" ")) {
            if (isDoneBad(lastExerciseHistory))
                audioFileMetaData.setSpeedSlowest()
            else
                audioFileMetaData.setSpeedSlow()
        } else if (isDoneBad(lastExerciseHistory)) {
            audioFileMetaData.setSpeedSlow()
        }
        return audioFileMetaData
    }

    fun isDoneBad(lastHistory: StudyHistory?): Boolean =
        lastHistory != null && !exerciseService.isDoneWell(lastHistory)

    fun isMultiWords(seriesType: ExerciseType): Boolean =
        seriesType == ExerciseType.PHRASES || seriesType == ExerciseType.SENTENCE || seriesType == ExerciseType.WORDS_SEQUENCES

    fun countWorkDaysForMonth(dayStudyStatistics: List<DayStudyStatistic>): Int =
        dayStudyStatistics
            .map { it.date }
            .groupBy { it.dayOfMonth }
            .keys.size
}
