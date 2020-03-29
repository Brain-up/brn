package com.epam.brn.upload.csv.processor

import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.upload.csv.record.GroupRecord
import org.springframework.stereotype.Component

@Component
class GroupRecordProcessor(private val groupRepository: ExerciseGroupRepository) {

    fun process(records: List<GroupRecord>): List<ExerciseGroup> {
        return groupRepository.saveAll(records.map { ExerciseGroup(it) }).toList()
    }
}
