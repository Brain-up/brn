package com.epam.brn.service

import com.epam.brn.dto.AudiometryDto
import com.epam.brn.enums.AudiometryType
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Audiometry
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AudiometryHistoryRepository
import com.epam.brn.repo.AudiometryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AudiometryService(
    private val audiometryRepository: AudiometryRepository,
    private val audiometryTaskRepository: AudiometryTaskRepository,
    private val audiometryHistoryRepository: AudiometryHistoryRepository,
    private val userAccountService: UserAccountService,
) {
    @Value("#{'\${frequencyForDiagnostic}'.split(',')}")
    lateinit var frequencyForDiagnostic: List<Int>

    fun getAudiometrics(locale: String): List<AudiometryDto> =
        audiometryRepository
            .findByLocale(locale)
            .map { a -> a.toDtoWithoutTasks() }

    fun getAudiometry(audiometryId: Long): AudiometryDto {
        val audiometry = audiometryRepository.findById(audiometryId)
            .orElseGet { throw EntityNotFoundException("No audiometry was found with id=$audiometryId") }
        val userTasks = getUserAudiometryTasks(audiometry)
        return audiometry.toDtoWithTasks(userTasks)
    }

    fun getUserAudiometryTasks(audiometry: Audiometry): List<AudiometryTask> {
        return when (audiometry.audiometryType) {
            AudiometryType.SIGNALS.name, AudiometryType.MATRIX.name -> audiometryTaskRepository.findByAudiometry(
                audiometry
            )
            AudiometryType.SPEECH.name -> {
                val user = userAccountService.getCurrentUser()
                findSecondSpeechAudiometryTasks(user, audiometry)
            }
            else -> throw IllegalArgumentException("Audiometry `$audiometry` does not supported in the system.")
        }
    }

    fun findSecondSpeechAudiometryTasks(user: UserAccount, audiometry: Audiometry): List<AudiometryTask> {
        val userHistory = audiometryHistoryRepository.findByUserAndAudiometry(user, audiometry)
        val mapZoneLastTask = userHistory
            .groupBy({ it.audiometryTask.frequencyZone }, { it })
            .map { mapZoneTasks -> Pair(mapZoneTasks.key, mapZoneTasks.value.maxByOrNull { it.startTime }) }
            .associate { it.first to it.second!!.audiometryTask }
        val mapZoneTasks = audiometry.audiometryTasks.groupBy({ it.frequencyZone }, { it })
        val nextTasks = mutableListOf<AudiometryTask>()
        mapZoneTasks.forEach { (zone, zoneTasks) ->
            run {
                val doneIndex = zoneTasks.indexOf(mapZoneLastTask[zone])
                val nextIndex = if (zoneTasks.size > doneIndex + 1) doneIndex + 1 else 0
                val secondTask = zoneTasks[nextIndex]
                nextTasks.add(secondTask)
            }
        }
        return nextTasks
    }
}
