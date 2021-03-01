package com.epam.brn.upload.csv.subgroup

import com.epam.brn.model.SubGroup
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.upload.csv.RecordProcessor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityNotFoundException

@Component
class SubgroupGenericRecordProcessor(
    private val seriesRepository: SeriesRepository,
    private val subGroupRepository: SubGroupRepository
) : RecordProcessor<SubgroupGenericRecord, SubGroup> {

    override fun isApplicable(record: Any): Boolean {
        return record is SubgroupGenericRecord
    }

    @Transactional
    override fun process(records: List<SubgroupGenericRecord>): List<SubGroup> {
        val subGroups = records
            .map {
                val series = seriesRepository
                    .findByType(it.seriesType)
                    ?: throw EntityNotFoundException("Series ${it.seriesType} was not found.")
                SubGroup(it, series)
            }
        subGroups.forEach { subGroup ->
            run {
                val existSubGroup = subGroupRepository.findByNameAndLevel(subGroup.name, subGroup.level)
                if (existSubGroup == null)
                    subGroupRepository.save(subGroup)
            }
        }
        return subGroups
    }
}
