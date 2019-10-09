package com.epam.brn.repo

import com.epam.brn.model.ExerciseGroup
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExerciseGroupRepository : CrudRepository<ExerciseGroup, Long> {

    fun findByNameLike(name: String): List<ExerciseGroup>
    fun findByIdLike(id: String): List<ExerciseGroup>
}