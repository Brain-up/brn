package com.epam.brn.converter

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.StudyHistory
import org.mapstruct.InjectionStrategy
import org.mapstruct.Mapper
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
interface StudyHistoryNotNullConverter {

    fun updateStudyHistory(studyHistoryDto: StudyHistoryDto, @MappingTarget studyHistory: StudyHistory)
}