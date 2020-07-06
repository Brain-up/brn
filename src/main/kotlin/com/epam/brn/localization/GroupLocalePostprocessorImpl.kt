package com.epam.brn.localization

import com.epam.brn.dto.ExerciseGroupDto
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

@Component
class GroupLocalePostprocessorImpl(private val messageSource: MessageSource) :
    LocalePostprocessor<ExerciseGroupDto> {

    private val mapOfMessages: Map<String, Pair<String, String>> = mapOf(
        "Неречевые упражнения" to Pair("group.first.name", "group.first.description"),
        "Речевые упражнения" to Pair("group.second.name", "group.second.description")
    )

    override fun postprocess(dto: ExerciseGroupDto): ExerciseGroupDto {
        val sourceName = mapOfMessages[dto.name]
        sourceName?.let {
            val locale = LocaleContextHolder.getLocale()
            val name = messageSource.getMessage(it.first, null, locale)
            val description = messageSource.getMessage(it.second, null, locale)
            dto.name = name
            dto.description = description
        }
        return dto
    }
}
