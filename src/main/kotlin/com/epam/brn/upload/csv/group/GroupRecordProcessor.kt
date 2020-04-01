package com.epam.brn.upload.csv.group

import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.upload.csv.RecordProcessor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GroupRecordProcessor(private val groupRepository: ExerciseGroupRepository) :
    RecordProcessor<GroupRecord, ExerciseGroup> {

    override fun isApplicable(record: Any): Boolean {
        return record is GroupRecord
    }

    @Transactional
    override fun process(records: List<GroupRecord>): List<ExerciseGroup> {
        return groupRepository.saveAll(records.map { ExerciseGroup(it) }).toList()
    }
}
