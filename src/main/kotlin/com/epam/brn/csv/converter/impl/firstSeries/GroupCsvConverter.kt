package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.csv.converter.Converter
import com.epam.brn.csv.dto.GroupCsv
import com.epam.brn.model.ExerciseGroup
import org.springframework.stereotype.Component

@Component
class GroupCsvConverter : Converter<GroupCsv, ExerciseGroup> {

    override fun convert(source: GroupCsv) =
        ExerciseGroup(name = source.name, description = source.description, id = source.groupId)
}
