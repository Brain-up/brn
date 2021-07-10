package com.epam.brn.service.statistic.impl

import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.service.statistic.progress.status.ProgressStatusManager
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserDayStatisticService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
    private val progressManager: ProgressStatusManager<List<StudyHistory>>
) : UserPeriodStatisticService<DayStudyStatistic> {
    override fun getStatisticForPeriod(from: LocalDateTime, to: LocalDateTime, userId: Long?): List<DayStudyStatistic> {
        val tempUserId = userId ?: userAccountService.getUserFromTheCurrentSession().id
        val studyHistories = studyHistoryRepository.findAllByUserAccountIdAndStartTimeBetween(
            userId = tempUserId!!,
            from = from,
            to = to
        )
        return studyHistories.map {
            val filteredStudyHistories = studyHistories.filter { studyHistoryFilter ->
                studyHistoryFilter.startTime.toLocalDate() == it.startTime.toLocalDate()
            }
            DayStudyStatistic(
                exercisingTimeSeconds = filteredStudyHistories.sumBy { dayStudyHistory -> dayStudyHistory.executionSeconds },
                date = it.startTime,
                progress = progressManager.getStatus(UserExercisingPeriod.DAY, filteredStudyHistories)
            )
        }.distinctBy { it.date.toLocalDate() }
    }
}
