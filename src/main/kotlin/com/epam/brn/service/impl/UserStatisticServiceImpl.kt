package com.epam.brn.service.impl

import com.epam.brn.dto.response.SubGroupStatisticDto
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserStatisticService
import org.springframework.stereotype.Service

/**
 *@author Nikolai Lazarev
 */

@Service
class UserStatisticServiceImpl(
    private val studyHistoryRepository: StudyHistoryRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userAccountService: UserAccountService
) : UserStatisticService {
    override fun getSubGroupStatistic(subGroupsIds: List<Long>): List<SubGroupStatisticDto> {
        val userAccount = userAccountService.getUserFromTheCurrentSession()
        return subGroupsIds.map {
            SubGroupStatisticDto(
                it,
                exerciseRepository.findExercisesBySubGroupId(it).size,
                studyHistoryRepository.getDoneExercises(it, userAccount.id!!).size
            )
        }.toList()
    }
}
