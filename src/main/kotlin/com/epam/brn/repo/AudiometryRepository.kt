package com.epam.brn.repo

import com.epam.brn.model.Audiometry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AudiometryRepository : JpaRepository<Audiometry, Long> {
    fun findByAudiometryTypeAndLocale(audiometryType: String, locale: String): Audiometry?
    fun findByAudiometryType(audiometryType: String): List<Audiometry>
    fun findByLocale(locale: String): List<Audiometry>
    fun findByName(locale: String): Optional<Audiometry>

    @Query("select distinct a from Audiometry a left JOIN FETCH a.audiometryTasks where a.id = ?1")
    fun findByIdWithTasks(id: Long): Optional<Audiometry>
}
