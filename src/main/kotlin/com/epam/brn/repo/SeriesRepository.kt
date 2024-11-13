package com.epam.brn.repo

import com.epam.brn.model.Series
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SeriesRepository : CrudRepository<Series, Long> {

    fun findByNameLike(name: String): List<Series>

    @Query("select distinct s from Series s left join fetch s.exerciseGroup where s.type = :type and s.exerciseGroup.locale = :locale")
    fun findByTypeAndLocale(type: String, locale: String): Series?

    fun findByTypeAndName(type: String, name: String): Series?

    fun findByNameIn(names: List<String>): List<Series>

    @Query("select distinct s from Series s left join fetch s.exerciseGroup where s.exerciseGroup.id = :groupId")
    fun findByExerciseGroupLike(groupId: Long): List<Series>

    @Query("select distinct s from Series s left join fetch s.exercises where s.id = :seriesId")
    fun findSeriesWithExercisesById(seriesId: Long): Optional<Series>
}
