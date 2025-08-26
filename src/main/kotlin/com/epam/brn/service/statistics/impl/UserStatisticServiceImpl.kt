package com.epam.brn.service.statistics.impl

import com.epam.brn.dto.response.SubGroupStatisticsResponse
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistics.UserStatisticService
import org.springframework.stereotype.Service

/**
 *@author Nikolai Lazarev
 */

@Service
class UserStatisticServiceImpl(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userAccountService: UserAccountService,
) : UserStatisticService<SubGroupStatisticsResponse> {
    override fun getSubGroupStatistic(subGroupsIds: List<Long>): List<SubGroupStatisticsResponse> {
        val userAccount = userAccountService.getCurrentUserDto()
        return subGroupsIds
            .map {
                SubGroupStatisticsResponse(
                    subGroupId = it,
                    totalExercises = exerciseRepository.findExercisesBySubGroupId(it).size,
                    completedExercises = studyHistoryRepository.getDoneExercises(it, userAccount.id!!).size,
                )
            }.toList()
    }
}
