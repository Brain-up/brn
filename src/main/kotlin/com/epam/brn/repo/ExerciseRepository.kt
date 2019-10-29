package com.epam.brn.repo

import com.epam.brn.model.Exercise
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExerciseRepository : CrudRepository<Exercise, Long> {

    fun findByNameLike(name: String): List<Exercise>
}