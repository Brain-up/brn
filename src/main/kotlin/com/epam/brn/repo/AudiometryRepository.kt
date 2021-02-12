package com.epam.brn.repo

import com.epam.brn.model.Audiometry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AudiometryRepository : JpaRepository<Audiometry, Long> {
    fun findByAudiometryTypeAndLocale(audiometryType: String, locale: String): Audiometry?
    fun findByLocale(locale: String): List<Audiometry>
    fun findByLocaleIsNull(): List<Audiometry>
}
