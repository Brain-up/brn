package com.epam.brn.integration.repo

import com.epam.brn.model.ExerciseGroup
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExerciseGroupRepository : CrudRepository<ExerciseGroup, Long> {
    override fun findAll(): List<ExerciseGroup>
}
