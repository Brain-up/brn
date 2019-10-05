package com.epam.brn.repo

import com.epam.brn.model.ExerciseSeries
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExerciseSeriesRepository : CrudRepository<ExerciseSeries, Long> {

    fun findByNameLike(name: String): List<ExerciseSeries>
    fun findByIdLike(id: String): List<ExerciseSeries>
}