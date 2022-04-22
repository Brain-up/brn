package com.epam.brn.service.impl

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.TextToSpeechService
import com.epam.brn.service.TimeService
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
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val timeService: TimeService,
    private val textToSpeechService: TextToSpeechService
) : UserAnalyticsService {

    override fun getUsersWithAnalytics(pageable: Pageable, role: String): List<UserWithAnalyticsResponse> {
        val users = userAccountRepository.findUsersAccountsByRole(role).map { it.toAnalyticsDto() }

        val now = timeService.now()
        val firstWeekDay = WeekFields.of(Locale.getDefault()).dayOfWeek()
        val startDay = now.with(firstWeekDay, 1L)
        val from = startDay.with(LocalTime.MIN)
        val to = startDay.plusDays(7L).with(LocalTime.MAX)

        users.onEach { it.lastWeek = userDayStatisticService.getStatisticForPeriod(from, to, it.id) }

        return users
    }

    override fun prepareAudioFileForUser(exerciseId: Long, audioFileMetaData: AudioFileMetaData): InputStream {
        // todo add later logic how to generate audio base on user study history
        return textToSpeechService.generateAudioOggFileWithValidation(audioFileMetaData)
    }
}
