package com.epam.brn.repo

import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SubGroupRepository : CrudRepository<SubGroup, Long> {

    fun findByNameLike(name: String): List<SubGroup>

    @Query("select distinct s from SubGroup s join fetch s.series ser join fetch ser.exerciseGroup where s.code = ?1 and ser.exerciseGroup.locale = ?2")
    fun findByCodeAndLocale(code: String, locale: String): SubGroup?

    fun findByNameAndLevelAndSeries(name: String, level: Int, series: Series): SubGroup?

    fun findByNameAndLevel(name: String, level: Int): SubGroup?

    @Query("select distinct s from SubGroup s join fetch s.series ser join fetch ser.exerciseGroup where s.name = ?1 and s.level = ?2 and ser.exerciseGroup.locale = ?3")
    fun findByNameAndLevelAndLocale(name: String, level: Int, locale: String): SubGroup?

    @Query("select distinct s from SubGroup s join fetch s.series where s.series.id = ?1")
    fun findBySeriesId(seriesId: Long): List<SubGroup>
}
