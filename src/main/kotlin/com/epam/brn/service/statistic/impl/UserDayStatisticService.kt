package com.epam.brn.service.statistic.impl

import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserDayStatisticService(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
) : UserPeriodStatisticService<DayStudyStatistic> {
    override fun getStatisticForPeriod(from: LocalDateTime, to: LocalDateTime, userId: Long?): List<DayStudyStatistic> {
        val tempUserId = userId ?: userAccountService.getUserFromTheCurrentSession().id
        val studyHistories = studyHistoryRepository.findAllByUserAccountIdAndStartTimeBetween(
            userId = tempUserId!!,
            from = from,
            to = to
        )
        return studyHistories.map { studyHistory ->
            DayStudyStatistic(
                exercisingTimeSeconds = studyHistories.filter { filterStudyHistory ->
                    filterStudyHistory.startTime.toLocalDate() == studyHistory.startTime.toLocalDate()
                }.map {
                    it.executionSeconds
                }.sum(),
                date = studyHistory.startTime,
                progress = UserExercisingProgressStatus.GREAT
            )
        }.distinctBy { it.date.toLocalDate() }
    }
}
