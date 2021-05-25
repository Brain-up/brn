package com.epam.brn.upload.csv.group

import com.epam.brn.enums.Locale
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
    override fun process(records: List<GroupRecord>, locale: Locale): List<ExerciseGroup> {
        val groups = records
            .map {
                ExerciseGroup(it)
            }
        groups.forEach { group ->
            run {
                val existGroup = groupRepository.findByCode(group.code)
                    .orElse(null)
                if (existGroup == null)
                    groupRepository.save(group)
            }
        }
        return groups
    }
}
