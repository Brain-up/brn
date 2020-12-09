package com.epam.brn.repo

import com.epam.brn.model.ExerciseGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ExerciseGroupRepository : JpaRepository<ExerciseGroup, Long> {
    fun findByLocale(locale: String): List<ExerciseGroup>
}
