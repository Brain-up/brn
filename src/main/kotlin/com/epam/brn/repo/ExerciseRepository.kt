package com.epam.brn.repo

import com.epam.brn.model.Exercise
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ExerciseRepository : JpaRepository<Exercise, Long> {

    fun findExercisesBySubGroupId(subGroupId: Long): List<Exercise>

    fun findByNameAndLevel(name: String, level: Int): Exercise?

    fun findExerciseByNameAndLevel(name: String, level: Int): Optional<Exercise>

    override fun findById(id: Long): Optional<Exercise>
}
