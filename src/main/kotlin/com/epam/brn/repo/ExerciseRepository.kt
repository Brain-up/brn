package com.epam.brn.repo

import com.epam.brn.model.Exercise
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ExerciseRepository : JpaRepository<Exercise, Long> {
    fun findExercisesBySeriesId(seriesId: Long): List<Exercise>
    override fun findById(seriesId: Long): Optional<Exercise>
}
