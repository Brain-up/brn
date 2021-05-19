package com.epam.brn.service.statistic.impl

import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.service.statistic.progress.status.ProgressStatusManager
import org.springframework.stereotype.Service
import java.sql.Date
import java.time.LocalDate

/**
 *@author Nikolai Lazarev
 */

@Service
class UserDayStatisticService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
    private val progressManager: ProgressStatusManager<List<StudyHistory>>
) : UserPeriodStatisticService<DayStudyStatistic> {
    override fun getStatisticForPeriod(from: LocalDate, to: LocalDate, userId: Long?): List<DayStudyStatistic> {
        val tempUserId = userId ?: userAccountService.getUserFromTheCurrentSession().id
        val studyHistories = studyHistoryRepository.getHistories(
            tempUserId!!,
            Date.valueOf(from),
            Date.valueOf(to)
        )
        return studyHistories.map {
            val filteredStudyHistories = studyHistories.filter { studyHistoryFilter ->
                studyHistoryFilter.startTime.toLocalDate() == it.startTime.toLocalDate()
            }
            DayStudyStatistic(
                exercisingTimeSeconds = filteredStudyHistories.sumBy { dayStudyHistory -> dayStudyHistory.executionSeconds },
                date = it.startTime.toLocalDate(),
                progress = progressManager.getStatus(UserExercisingPeriod.DAY, filteredStudyHistories)
            )
        }.distinct()
    }
}
