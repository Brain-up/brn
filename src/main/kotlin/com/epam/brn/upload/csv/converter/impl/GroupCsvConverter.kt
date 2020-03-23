package com.epam.brn.upload.csv.converter.impl

import com.epam.brn.model.ExerciseGroup
import com.epam.brn.upload.csv.converter.Converter
import com.epam.brn.upload.csv.record.GroupRecord
import org.springframework.stereotype.Component

@Component
class GroupCsvConverter : Converter<GroupRecord, ExerciseGroup> {

    override fun convert(source: GroupRecord) =
        ExerciseGroup(name = source.name, description = source.description, id = source.groupId)
}
