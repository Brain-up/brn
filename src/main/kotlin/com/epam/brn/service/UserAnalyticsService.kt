package com.epam.brn.service

import com.epam.brn.dto.response.UserWithAnalyticsResponse
import org.springframework.data.domain.Pageable

interface UserAnalyticsService {
    fun getUsersWithAnalytics(pageable: Pageable, role: String): List<UserWithAnalyticsResponse>
}
