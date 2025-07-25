package com.epam.brn.service.statistics.progress.status.impl

import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistics.progress.status.UserRestTimeRetriever
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.TreeSet

@Component
class UserRestTimeRetrieverImpl(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val userAccountService: UserAccountService,
    private val studyHistoryTimeComparator: Comparator<StudyHistory>,
) : UserRestTimeRetriever {
    override fun getMaximalUserRestTime(
        userId: Long?,
        from: LocalDate,
        to: LocalDate,
    ): Int {
        val userTempId = userId ?: userAccountService.getCurrentUserDto().id
        val histories =
            studyHistoryRepository.getHistories(
                userTempId!!,
                from.atStartOfDay(),
                to.atStartOfDay(),
            )
        val period = TreeSet(studyHistoryTimeComparator)
        period.addAll(histories)
        val coolDowns: ArrayList<Int> = ArrayList()
        for (i in 1 until period.size) {
            coolDowns.add(
                ChronoUnit.DAYS.between(period.elementAt(i - 1).startTime, period.elementAt(i).startTime).toInt(),
            )
        }
        return coolDowns.maxOrNull() ?: 0
    }
}
