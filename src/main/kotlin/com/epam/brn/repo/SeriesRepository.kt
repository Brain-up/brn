package com.epam.brn.repo

import com.epam.brn.model.Series
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SeriesRepository : CrudRepository<Series, Long> {

    fun findByNameLike(name: String): List<Series>
    fun findByIdLike(id: String): List<Series>

    @Query("select distinct s from Series s where s.exerciseGroup.id=?1")
    fun findByExerciseGroupLike(groupId: Long): List<Series>
}