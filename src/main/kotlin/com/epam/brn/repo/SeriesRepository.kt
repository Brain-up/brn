package com.epam.brn.repo

import com.epam.brn.model.Series
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SeriesRepository : CrudRepository<Series, Long> {
    fun findByNameLike(name: String): List<Series>

    @Query("select distinct s from Series s where s.type=?1 and s.exerciseGroup.locale=?2")
    fun findByTypeAndLocale(
        type: String,
        locale: String,
    ): Series?

    fun findByTypeAndName(
        type: String,
        name: String,
    ): Series?

    fun findByNameIn(names: List<String>): List<Series>

    fun findDistinctByExerciseGroupIdAndActiveTrue(groupId: Long): List<Series>

    fun findByIdAndActiveTrue(seriesId: Long): Series?

//    @Query("select distinct s from Series s left JOIN FETCH s.exercises where s.id=?1")
//    fun findSeriesWithExercisesById(groupId: Long): Optional<Series>
}
