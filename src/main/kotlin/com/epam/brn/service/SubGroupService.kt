package com.epam.brn.service

import com.epam.brn.dto.SubGroupDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.integration.repo.SubGroupRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class SubGroupService(private val subGroupRepository: SubGroupRepository) {

    private val log = logger()

    fun findSubGroupsForSeries(seriesId: Long): List<SubGroupDto> {
        log.debug("Try to find subGroups for seriesId=$seriesId")
        val subGroups = subGroupRepository.findBySeriesId(seriesId)
        return subGroups.map { subGroup -> subGroup.toDto() }
    }

    fun findById(subGroupId: Long): SubGroupDto {
        log.debug("try to find SubGroup by Id=$subGroupId")
        val series = subGroupRepository.findById(subGroupId)
            .orElseThrow { EntityNotFoundException("No subGroup was found by id=$subGroupId") }
        return series.toDto()
    }
}
