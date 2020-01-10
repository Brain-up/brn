package com.epam.brn.converter

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.StudyHistory
import org.mapstruct.BeanMapping
import org.mapstruct.InjectionStrategy
import org.mapstruct.Mapper
import org.mapstruct.MappingTarget
import org.mapstruct.NullValueCheckStrategy
import org.mapstruct.NullValuePropertyMappingStrategy

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
interface StudyHistoryConverter {
    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    fun updateStudyHistory(studyHistoryDto: StudyHistoryDto, @MappingTarget studyHistory: StudyHistory)

    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    fun updateStudyHistoryWhereNotNull(studyHistoryDto: StudyHistoryDto, @MappingTarget studyHistory: StudyHistory)
}
