package com.epam.brn.repo

import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SubGroupRepository : CrudRepository<SubGroup, Long> {

    fun findByNameLike(name: String): List<SubGroup>

    fun findByCode(code: String): SubGroup

    fun findByNameAndLevelAndSeries(name: String, level: Int, series: Series): SubGroup?

    @Query("select distinct s from SubGroup s where s.series.id=?1")
    fun findBySeriesId(seriesId: Long): List<SubGroup>
}
