package com.epam.brn.service

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class ExerciseGroupsService(
    @Autowired val exerciseGroupRepository: ExerciseGroupRepository
) {
    private val log = logger()

    fun findAllGroups(): List<ExerciseGroupDto> {
        log.debug("Searching all groups")
        val groups: List<ExerciseGroup> = exerciseGroupRepository.findAll()
        return groups.mapNotNull { group -> group.toDto() }
    }

    fun findGroupDtoById(groupId: Long): ExerciseGroupDto {
        log.debug("Searching group with id=$groupId")
        val group: Optional<ExerciseGroup> = exerciseGroupRepository.findById(groupId)
        return group.map { x -> x.toDto() }
            .orElseThrow { EntityNotFoundException("no group was found for id=$groupId") }
    }

    fun findGroupById(groupId: Long): ExerciseGroup {
        log.debug("Searching group with id=$groupId")
        return exerciseGroupRepository.findById(groupId)
            .orElseThrow { EntityNotFoundException("no group was found for id=$groupId") }
    }

    fun findByLocale(locale: String): List<ExerciseGroupDto> {
        log.debug("Searching groups by locale=$locale")
        if (locale.isEmpty())
            return exerciseGroupRepository.findAll().map { group -> group.toDto() }
        return exerciseGroupRepository.findByLocale(locale)
            .map { group -> group.toDto() }
    }

    fun save(exerciseGroup: ExerciseGroup): ExerciseGroup {
        return exerciseGroupRepository.save(exerciseGroup)
    }
}
