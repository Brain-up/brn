package com.epam.brn.service.impl

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.dto.statistics.DayStudyStatistics
import com.epam.brn.enums.ExerciseType
import com.epam.brn.enums.Voice
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
import com.epam.brn.service.statistics.UserPeriodStatisticsService
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
    private val userDayStatisticsService: UserPeriodStatisticsService<DayStudyStatistics>,
    private val timeService: TimeService,
    private val textToSpeechService: TextToSpeechService,
    private val userAccountService: UserAccountService,
    private val exerciseService: ExerciseService,
) : UserAnalyticsService {

    private val listTextExercises = listOf(ExerciseType.SENTENCE, ExerciseType.PHRASES)

    override fun getUsersWithAnalytics(pageable: Pageable, role: String): List<UserWithAnalyticsResponse> {
        val users = userAccountRepository.findUsersAccountsByRole(role).map { it.toAnalyticsDto() }

        val now = timeService.now()
        val firstWeekDay = WeekFields.of(Locale.getDefault()).dayOfWeek()
        val startDay = now.with(firstWeekDay, 1L)
        val from = startDay.with(LocalTime.MIN)
        val to = startDay.plusDays(7L).with(LocalTime.MAX)
        val startOfCurrentMonth = now.withDayOfMonth(1).with(LocalTime.MIN)

        users.onEach { user ->
            user.lastWeek = userDayStatisticsService.getStatisticsForPeriod(from, to, user.id)
            user.studyDaysInCurrentMonth = countWorkDaysForMonth(
                userDayStatisticsService.getStatisticsForPeriod(startOfCurrentMonth, now, user.id)
            )

            val userStatistic = studyHistoryRepository.getStatisticsByUserAccountId(user.id)
            user.apply {
                this.firstDone = userStatistic.firstStudy
                this.lastDone = userStatistic.lastStudy
                this.spentTime = userStatistic.spentTime.toDuration(DurationUnit.SECONDS)
                this.doneExercises = userStatistic.doneExercises
            }
        }
        return users
    }

    override fun prepareAudioStreamForUser(exerciseId: Long, audioFileMetaData: AudioFileMetaData): InputStream =
        textToSpeechService
            .generateAudioOggStreamWithValidation(
                prepareAudioFileMetaData(exerciseId, audioFileMetaData)
            )

    override fun prepareAudioFileMetaData(exerciseId: Long, audioFileMetaData: AudioFileMetaData): AudioFileMetaData {
        val seriesType = ExerciseType.valueOf(exerciseRepository.findTypeByExerciseId(exerciseId))
        val text = audioFileMetaData.text
        if (!listTextExercises.contains(seriesType))
            audioFileMetaData.text = text.replace(" ", ", ")
        val currentUser = userAccountService.getCurrentUser()
        // todo use choseVoiceForUser(currentUser) after moving to yandex speechKit v3
        audioFileMetaData.voice = Voice.marina.name
        setSpeedForUser(currentUser, exerciseId, audioFileMetaData)
        return audioFileMetaData
    }

    fun setSpeedForUser(user: UserAccount, exerciseId: Long, audioFileMetaData: AudioFileMetaData) {
        val lastExerciseHistory = studyHistoryRepository
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
            return Voice.marina.name
        val ages = LocalDate.now().year - user.bornYear!!
        return if (ages < 19) Voice.marina.name
        else Voice.lera.name
    }

    fun isDoneBad(lastHistory: StudyHistory?): Boolean =
        lastHistory != null && !exerciseService.isDoneWell(lastHistory)

    fun isDoneWell(lastHistory: StudyHistory?): Boolean =
        lastHistory != null && exerciseService.isDoneWell(lastHistory)

    fun isMultiWords(seriesType: ExerciseType): Boolean =
        seriesType == ExerciseType.PHRASES || seriesType == ExerciseType.SENTENCE || seriesType == ExerciseType.WORDS_SEQUENCES

    fun countWorkDaysForMonth(dayStudyStatistics: List<DayStudyStatistics>): Int =
        dayStudyStatistics
            .map { it.date }
            .groupBy { it.dayOfMonth }
            .keys.size
}
