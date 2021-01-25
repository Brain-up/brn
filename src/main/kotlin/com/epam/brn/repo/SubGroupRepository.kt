package com.epam.brn.repo

import com.epam.brn.model.SubGroup
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SubGroupRepository : CrudRepository<SubGroup, Long> {

    fun findByNameLike(name: String): List<SubGroup>

    fun findByCode(code: String): SubGroup

    @Query("select distinct s from SubGroup s where s.series.id=?1")
    fun findBySeriesId(seriesId: Long): List<SubGroup>

//    @Query("select distinct s from Series s left JOIN FETCH s.exercises where s.id=?1")
//    fun findSeriesWithExercisesById(groupId: Long): Optional<Series>
}
