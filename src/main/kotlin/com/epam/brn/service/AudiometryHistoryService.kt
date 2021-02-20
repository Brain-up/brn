package com.epam.brn.service

import com.epam.brn.dto.request.AudiometryHistoryRequest
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.repo.AudiometryHistoryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import org.springframework.stereotype.Service

@Service
class AudiometryHistoryService(
    private val audiometryHistoryRepository: AudiometryHistoryRepository,
    private val audiometryTaskRepository: AudiometryTaskRepository,
    private val userAccountService: UserAccountService,
) {
    fun save(request: AudiometryHistoryRequest): Long {
        val currentUser = userAccountService.getCurrentUser()
        val audiometryTask = audiometryTaskRepository
            .findById(request.audiometryTaskId)
            .orElseThrow { EntityNotFoundException("AudiometryTask with id=$request.audiometryTaskId was not found!") }
        val audiometryHistory = request.toEntity(currentUser, audiometryTask)
        return audiometryHistoryRepository.save(audiometryHistory).id!!
    }
}
