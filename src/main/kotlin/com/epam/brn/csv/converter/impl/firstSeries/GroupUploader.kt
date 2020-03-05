package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.csv.converter.Uploader
import com.epam.brn.csv.dto.GroupCsv
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.springframework.stereotype.Component

@Component
class GroupUploader(
    private val exerciseGroupRepository: ExerciseGroupRepository
) : Uploader<GroupCsv, ExerciseGroup> {

    override fun persistEntity(entity: ExerciseGroup) {
        exerciseGroupRepository.save(entity)
    }

    override fun entityComparator(): (ExerciseGroup) -> Int {
        return { it.id?.toInt()!! }
    }

    override fun objectReader(): ObjectReader {
        val csvMapper = CsvMapper().apply {
            enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.TRIM_SPACES)
        }

        val csvSchema = csvMapper
            .schemaFor(GroupCsv::class.java)
            .withColumnSeparator(',')
            .withLineSeparator(" ")
            .withColumnReordering(true)
            .withHeader()

        return csvMapper
                .readerWithTypedSchemaFor(GroupCsv::class.java)
                .with(csvSchema)
    }

    override fun convert(source: GroupCsv) =
        ExerciseGroup(name = source.name, description = source.description, id = source.groupId)
}
