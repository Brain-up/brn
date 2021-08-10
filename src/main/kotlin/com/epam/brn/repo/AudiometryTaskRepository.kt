package com.epam.brn.repo

import com.epam.brn.model.Audiometry
import com.epam.brn.model.AudiometryTask
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface AudiometryTaskRepository : JpaRepository<AudiometryTask, Long> {
    fun findByAudiometry(audiometry: Audiometry): List<AudiometryTask>
    fun countByAudiometry(audiometry: Audiometry): Long
    @Transactional
    fun removeAllByAudiometry(audiometry: Audiometry): List<AudiometryTask>
    fun findByAudiometryAndFrequencies(audiometry: Audiometry, frequencies: String): AudiometryTask?
    fun findByAudiometryAndFrequencyZoneAndAudiometryGroup(
        audiometry: Audiometry,
        frequencyZone: String,
        audiometryGroup: String
    ): AudiometryTask?
}
