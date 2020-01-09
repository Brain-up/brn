package com.epam.brn.service

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import java.util.Optional
import org.apache.commons.collections4.CollectionUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ExerciseGroupsService(
    @Autowired val exerciseGroupRepository: ExerciseGroupRepository
) {
    private val log = logger()

    fun findAllGroups(): List<ExerciseGroupDto> {
        log.debug("Searching all groups")
        val groups: List<ExerciseGroup> = exerciseGroupRepository.findAll()
        return CollectionUtils.emptyIfNull(groups).mapNotNull { x -> x.toDto() }
    }

    fun findGroupById(groupId: Long): ExerciseGroupDto {
        log.debug("Searching group with id=$groupId")
        val group: Optional<ExerciseGroup> = exerciseGroupRepository.findById(groupId)
        return group.map { x -> x.toDto() }
            .orElseThrow { NoDataFoundException("no group was found for id=$groupId") }
    }
}
