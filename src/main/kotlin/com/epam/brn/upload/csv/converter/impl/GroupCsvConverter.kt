package com.epam.brn.upload.csv.converter.impl

import com.epam.brn.model.ExerciseGroup
import com.epam.brn.upload.csv.converter.Converter
import com.epam.brn.upload.csv.dto.GroupCsv
import org.springframework.stereotype.Component

@Component
class GroupCsvConverter : Converter<GroupCsv, ExerciseGroup> {

    override fun convert(source: GroupCsv) =
        ExerciseGroup(name = source.name, description = source.description, id = source.groupId)
}
