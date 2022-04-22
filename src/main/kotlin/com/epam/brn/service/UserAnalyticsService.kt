package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import org.springframework.data.domain.Pageable
import java.io.InputStream

interface UserAnalyticsService {
    fun getUsersWithAnalytics(pageable: Pageable, role: String): List<UserWithAnalyticsResponse>
    fun prepareAudioFileForUser(exerciseId: Long, audioFileMetaData: AudioFileMetaData): InputStream
}
