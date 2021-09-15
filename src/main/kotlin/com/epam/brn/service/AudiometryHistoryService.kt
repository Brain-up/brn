package com.epam.brn.service

import com.epam.brn.dto.request.AudiometryHistoryRequest
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Headphones
import com.epam.brn.model.SinAudiometryResult
import com.epam.brn.repo.AudiometryHistoryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import com.epam.brn.repo.SinAudiometryResultRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class AudiometryHistoryService(
    private val audiometryHistoryRepository: AudiometryHistoryRepository,
    private val audiometryTaskRepository: AudiometryTaskRepository,
    private val sinAudiometryResultRepository: SinAudiometryResultRepository,
    private val userAccountService: UserAccountService,
) {
    @Transactional(rollbackOn = [Exception::class])
    fun save(request: AudiometryHistoryRequest): Long {
        val currentUser = userAccountService.getCurrentUser()
        val audiometryTask = audiometryTaskRepository
            .findById(request.audiometryTaskId!!)
            .orElseThrow { EntityNotFoundException("AudiometryTask with id=$request.audiometryTaskId was not found!") }
        val headphonesFromUser = getSpecificHeadphonesFromCurrentUser(currentUser.headphones, request.headphones)
        val audiometryHistory = request.toEntity(currentUser, audiometryTask, headphonesFromUser)
        val savedAudiometryHistory = audiometryHistoryRepository.save(audiometryHistory)
        if (!request.sinAudiometryResults.isNullOrEmpty()) {
            request.sinAudiometryResults!!.forEach { (frequency, sound) ->
                sinAudiometryResultRepository.save(
                    SinAudiometryResult(frequency = frequency, soundLevel = sound, audiometryHistory = savedAudiometryHistory)
                )
            }
        }
        return savedAudiometryHistory.id!!
    }

    private fun getSpecificHeadphonesFromCurrentUser(headphones: MutableSet<Headphones>, headphonesId: Long?) =
        headphones.find() { entity ->
            entity.id == headphonesId
        } ?: throw IllegalArgumentException("Current user has ho headphones with id=$headphonesId")
}
