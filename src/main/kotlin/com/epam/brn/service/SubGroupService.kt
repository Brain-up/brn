package com.epam.brn.service

import com.epam.brn.dto.request.SubGroupChangeRequest
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.dto.response.SubGroupResponse
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.SubGroup
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class SubGroupService(
    private val subGroupRepository: SubGroupRepository,
    private val seriesRepository: SeriesRepository,
    private val exerciseRepository: ExerciseRepository,
    private val urlConversionService: UrlConversionService
) {
    private val log = logger()

    @Cacheable("subgroupsBySeriesId")
    fun findSubGroupsForSeries(seriesId: Long): List<SubGroupResponse> {
        return subGroupRepository
            .findBySeriesId(seriesId)
            .map { subGroup -> toSubGroupResponse(subGroup) }
            .sortedWith(compareBy({ it.level }, { it.withPictures }))
    }

    @Cacheable("subgroupsBySubGroupId")
    fun findById(subGroupId: Long): SubGroupResponse {
        val subGroup = subGroupRepository.findById(subGroupId)
            .orElseThrow { EntityNotFoundException("No subGroup was found by id=$subGroupId.") }
        return toSubGroupResponse(subGroup)
    }

    @CacheEvict("subgroupsBySeriesId", "subgroupsBySubGroupId")
    fun deleteSubGroupById(subGroupId: Long) {
        if (subGroupRepository.existsById(subGroupId)) {
            if (exerciseRepository.existsBySubGroupId(subGroupId))
                throw IllegalArgumentException("Can not delete subGroup because there are exercises that refer to the subGroup.")
            subGroupRepository.deleteById(subGroupId)
        } else
            throw IllegalArgumentException("Can not delete subGroup because subGroup is not found by this id.")
    }

    @CachePut("subgroupsBySeriesId", "subgroupsBySubGroupId")
    fun updateSubGroupById(subGroupId: Long, subGroupChangeRequest: SubGroupChangeRequest): SubGroupResponse {
        val subGroup = subGroupRepository.findById(subGroupId)
            .orElseThrow { EntityNotFoundException("Can not update subGroup because subGroup is not found by this id.") }
        subGroup.withPictures = subGroupChangeRequest.withPictures
        subGroupRepository.save(subGroup)
        return toSubGroupResponse(subGroup)
    }

    fun addSubGroupToSeries(subGroupRequest: SubGroupRequest, seriesId: Long): SubGroupResponse {
        log.debug("try to find subgroup by name=${subGroupRequest.name} and the level=${subGroupRequest.level}")
        val level = subGroupRequest.level ?: throw IllegalArgumentException("Level is required")
        val existSubGroup = subGroupRepository.findByNameAndLevel(subGroupRequest.name, level)
        if (existSubGroup != null)
            throw IllegalArgumentException("The subgroup with name=${subGroupRequest.name} and the level=$level already exists!")
        val series = seriesRepository.findById(seriesId)
            .orElseThrow { EntityNotFoundException("No series was found by id=$seriesId.") }
        return toSubGroupResponse(subGroupRepository.save(subGroupRequest.toModel(series)))
    }

    fun toSubGroupResponse(subGroup: SubGroup): SubGroupResponse {
        val pictureUrl = urlConversionService.makeUrlForSubGroupPicture(subGroup.code)
        return subGroup.toResponse(pictureUrl)
    }
}
