package com.epam.brn.service.statistic.impl

import com.epam.brn.dto.response.SubGroupStatisticResponse
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserStatisticService
import org.springframework.stereotype.Service

/**
 *@author Nikolai Lazarev
 */

@Service
class UserStatisticServiceImpl(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userAccountService: UserAccountService,
) : UserStatisticService<SubGroupStatisticResponse> {

    override fun getSubGroupStatistic(subGroupsIds: List<Long>): List<SubGroupStatisticResponse> {
        val userAccount = userAccountService.getUserFromTheCurrentSession()
        return subGroupsIds.map {
            SubGroupStatisticResponse(
                subGroupId = it,
                totalExercises = exerciseRepository.findExercisesBySubGroupId(it).size,
                completedExercises = studyHistoryRepository.getDoneExercises(it, userAccount.id!!).size
            )
        }.toList()
    }
}
