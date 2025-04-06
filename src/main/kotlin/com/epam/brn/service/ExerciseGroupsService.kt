package com.epam.brn.service

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class ExerciseGroupsService(
    @Autowired val exerciseGroupRepository: ExerciseGroupRepository,
) {
    private val log = logger()

    @Cacheable("groupsById")
    fun findGroupDtoById(groupId: Long): ExerciseGroupDto {
        log.debug("Searching group with id=$groupId")
        val group: Optional<ExerciseGroup> = exerciseGroupRepository.findById(groupId)
        return group
            .map { it.toDto() }
            .orElseThrow { EntityNotFoundException("No group was found for id=$groupId") }
    }

    @Cacheable("groupsByLocale")
    fun findByLocale(locale: String): List<ExerciseGroupDto> =
        if (locale.isEmpty()) {
            exerciseGroupRepository.findAll().map { group -> group.toDtoWithoutSeries() }
        } else {
            exerciseGroupRepository
                .findByLocale(locale)
                .map { group -> group.toDtoWithoutSeries() }
        }

    fun findGroupByCode(groupCode: String): ExerciseGroup {
        log.debug("Searching group with code=$groupCode")
        return exerciseGroupRepository
            .findByCode(groupCode)
            .orElseThrow { EntityNotFoundException("No group was found for code=$groupCode") }
    }
}
