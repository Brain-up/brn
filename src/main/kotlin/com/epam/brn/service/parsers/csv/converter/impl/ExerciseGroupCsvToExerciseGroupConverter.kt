package com.epam.brn.service.parsers.csv.converter.impl

import com.epam.brn.model.ExerciseGroup
import com.epam.brn.service.parsers.csv.converter.Converter
import com.epam.brn.service.parsers.csv.dto.GroupCsv
import org.springframework.stereotype.Component

@Component
class ExerciseGroupCsvToExerciseGroupConverter : Converter<GroupCsv, ExerciseGroup> {

    override fun convert(source: GroupCsv): ExerciseGroup {
        return ExerciseGroup(name = source.name, description = source.description)
    }
}
