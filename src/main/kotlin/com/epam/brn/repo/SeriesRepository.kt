package com.epam.brn.repo

import com.epam.brn.model.Series
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SeriesRepository : CrudRepository<Series, Long> {
    @Query("select distinct s from Series s where s.name LIKE :name and s.active = TRUE")
    fun findByNameLike(
        @Param("name") name: String,
    ): List<Series>

    @Query("select distinct s from Series s where s.type=?1 and s.exerciseGroup.locale=?2 and s.active = TRUE")
    fun findByTypeAndLocale(
        type: String,
        locale: String,
    ): Series?

    @Query("select distinct s from Series s where s.type=?1 and s.name=?2 and s.active = TRUE")
    fun findByTypeAndName(
        type: String,
        name: String,
    ): Series?

    @Query("select distinct s from Series s where s.name IN :names and s.active = TRUE")
    fun findByNameIn(
        @Param("names") names: List<String>,
    ): List<Series>

    @Query("select distinct s from Series s where s.exerciseGroup.id=?1 and s.active = TRUE")
    fun findByExerciseGroupLike(groupId: Long): List<Series>

    @Query("select distinct s from Series s where s.id=?1 and s.active = TRUE")
    fun findBySeriesId(seriesId: Long): Optional<Series>

//    @Query("select distinct s from Series s left JOIN FETCH s.exercises where s.id=?1")
//    fun findSeriesWithExercisesById(groupId: Long): Optional<Series>
}
