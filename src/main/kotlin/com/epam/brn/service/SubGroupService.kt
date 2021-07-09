package com.epam.brn.service

import com.epam.brn.dto.SubGroupDto
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.model.SubGroup
import com.epam.brn.repo.SeriesRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SubGroupService(
    private val subGroupRepository: SubGroupRepository,
    private val seriesRepository: SeriesRepository,
) {

    @Value(value = "\${brn.picture.theme.path}")
    private lateinit var pictureTheme: String

    private val log = logger()

    fun findSubGroupsForSeries(seriesId: Long): List<SubGroupDto> {
        log.debug("Try to find subGroups for seriesId=$seriesId")
        val subGroups = subGroupRepository.findBySeriesId(seriesId)
        return subGroups.map { subGroup -> subGroup.toDto(pictureTheme) }
    }

    fun findById(subGroupId: Long): SubGroupDto {
        log.debug("try to find SubGroup by Id=$subGroupId")
        val subGroup = subGroupRepository.findById(subGroupId)
            .orElseThrow { EntityNotFoundException("No subGroup was found by id=$subGroupId.") }
        return subGroup.toDto(pictureTheme)
    }

    fun addSubGroupToSeries(subGroupRequest: SubGroupRequest, seriesId: Long): SubGroupDto {
        log.debug("try to find subgroup by name=$subGroupRequest.name and the level=$subGroupRequest.level")
        val existSubGroup = subGroupRepository.findByNameAndLevel(subGroupRequest.name, subGroupRequest.level)
        if (existSubGroup != null) {
            throw IllegalArgumentException("The subgroup with name=$subGroupRequest.name and the level=$subGroupRequest.level already exists!")
        }
        log.debug("try to find Series by Id=$seriesId")
        val series = seriesRepository.findById(seriesId)
            .orElseThrow { EntityNotFoundException("No series was found by id=$seriesId.") }
        val subGroup = subGroupRequest.toModel(series)
        return subGroupRepository.save(subGroup).toDto()
    }
}

fun SubGroup.toDto(pictureUrlTemplate: String): SubGroupDto {
    val dto = this.toDto()
    val url = String.format(pictureUrlTemplate, dto.pictureUrl)
    dto.pictureUrl = url
    return dto
}
