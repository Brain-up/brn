package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.converter.DataLoadingBeanProvider
import com.epam.brn.csv.converter.ObjectReaderProvider
import com.epam.brn.csv.dto.GroupCsv
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import org.springframework.stereotype.Component

@Component
class GroupBeanProvider(
    private val repository: ExerciseGroupRepository,
    private val objectReaderProvider: ObjectReaderProvider<GroupCsv>,
    private val converter: CsvToEntityConverter<GroupCsv, ExerciseGroup>
) : DataLoadingBeanProvider<GroupCsv, ExerciseGroup> {
    override fun shouldProcess(fileName: String) = "groups.csv" == fileName
    override fun repository() = repository
    override fun objectReaderProvider() = objectReaderProvider
    override fun converter() = converter
}
