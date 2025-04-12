package com.epam.brn.service.impl

import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.repo.UserAnalyticsRepository
import com.epam.brn.service.UserAnalyticsServiceV1
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
class UserAnalyticsServiceV1Impl(
    private val userAnalyticsRepository: UserAnalyticsRepository,
) : UserAnalyticsServiceV1 {
    override fun getUsersWithAnalytics(
        pageable: Pageable,
        role: String,
    ): List<UserWithAnalyticsResponse> = userAnalyticsRepository.getUserAnalytics(pageable, role).map {
        UserWithAnalyticsResponse(
            id = it.id,
            userId = it.userId,
            name = it.fullName,
            active = it.active,
            email = it.email,
            bornYear = it.bornYear,
            gender = it.gender,
            firstDone = it.firstDone,
            lastDone = it.lastDone,
            lastVisit = it.lastVisit,
            doneExercises = it.doneExercises,
            spentTime = it.spentTime.toDuration(DurationUnit.SECONDS),
            studyDaysInCurrentMonth = it.studyDays,
        )
    }
}
