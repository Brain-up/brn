package com.epam.brn.service

import com.epam.brn.dto.SubGroupDto
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.model.SubGroup
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.ExerciseRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class SubGroupService(
    private val subGroupRepository: SubGroupRepository,
    private val seriesRepository: SeriesRepository,
    private val exerciseRepository: ExerciseRepository,
    private val urlConversionService: UrlConversionService
) {
    private val log = logger()

    fun findSubGroupsForSeries(seriesId: Long): List<SubGroupDto> {
        log.debug("Try to find subGroups for seriesId=$seriesId")
        val subGroups = subGroupRepository.findBySeriesId(seriesId)
        return subGroups.map { subGroup -> toSubGroupDto(subGroup) }
    }

    fun findById(subGroupId: Long): SubGroupDto {
        log.debug("try to find SubGroup by Id=$subGroupId")
        val subGroup = subGroupRepository.findById(subGroupId)
            .orElseThrow { EntityNotFoundException("No subGroup was found by id=$subGroupId.") }
        return toSubGroupDto(subGroup)
    }

    fun deleteSubGroupById(subGroupId: Long) {
        log.debug("try to delete SubGroup by Id=$subGroupId")
        if (subGroupRepository.existsById(subGroupId)) {
            if (exerciseRepository.existsBySubGroupId(subGroupId)) {
                throw IllegalArgumentException("Can not delete subGroup because there are exercises that refer to the subGroup.")
            }
            subGroupRepository.deleteById(subGroupId)
        } else {
            throw IllegalArgumentException("Can not delete subGroup because subGroup is not found by this id.")
        }
    }

    fun addSubGroupToSeries(subGroupRequest: SubGroupRequest, seriesId: Long): SubGroupDto {
        log.debug("try to find subgroup by name=${subGroupRequest.name} and the level=${subGroupRequest.level}")
        val existSubGroup = subGroupRepository.findByNameAndLevel(subGroupRequest.name, subGroupRequest.level)
        if (existSubGroup != null)
            throw IllegalArgumentException("The subgroup with name=${subGroupRequest.name} and the level=${subGroupRequest.level} already exists!")
        log.debug("try to find Series by Id=$seriesId")
        val series = seriesRepository.findById(seriesId)
            .orElseThrow { EntityNotFoundException("No series was found by id=$seriesId.") }
        val subGroup = subGroupRequest.toModel(series)
        val savedSubGroup = subGroupRepository.save(subGroup)
        return toSubGroupDto(savedSubGroup)
    }

    fun toSubGroupDto(subGroup: SubGroup): SubGroupDto {
        val pictureUrl = urlConversionService.makeUrlForSubGroupPicture(subGroup.code)
        return subGroup.toDto(pictureUrl)
    }
}
