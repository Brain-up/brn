package com.epam.brn.repo

import com.epam.brn.model.Audiometry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AudiometryRepository : JpaRepository<Audiometry, Long> {
    fun findByAudiometryType(audiometryType: String): Audiometry?
}
