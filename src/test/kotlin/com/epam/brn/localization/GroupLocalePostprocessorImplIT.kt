package com.epam.brn.localization

import com.epam.brn.dto.ExerciseGroupDto
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.test.context.ActiveProfiles
import java.util.Locale
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
internal class GroupLocalePostprocessorImplIT {
    @Autowired
    lateinit var groupLocalePostprocessor: GroupLocalePostprocessorImpl

    @ParameterizedTest(name = "Method with sourceDto {0} should return ExerciseGroupDto with name {1}")
    @MethodSource("getEnUsLocaleExerciseGroupsData")
    fun `should return ExerciseGroupDto with filled name from messages resource`(
        sourceDto: ExerciseGroupDto,
        expectedMessage: String,
    ) {
        LocaleContextHolder.setLocale(Locale(sourceDto.locale))
        val actualExerciseGroupDto: ExerciseGroupDto = groupLocalePostprocessor.postprocess(sourceDto)
        assertNotNull(actualExerciseGroupDto)
        assertEquals(expectedMessage, actualExerciseGroupDto.name)
    }

    companion object {
        @JvmStatic
        private fun getEnUsLocaleExerciseGroupsData() = listOf(
            Arguments.of(
                ExerciseGroupDto(
                    id = null,
                    locale = "en",
                    name = "Неречевые упражнения",
                    description = null,
                    series = mutableListOf(),
                ),
                "non-speech exercises",
            ),
            Arguments.of(
                ExerciseGroupDto(
                    id = null,
                    locale = "en",
                    name = "Речевые упражнения",
                    description = null,
                    series = mutableListOf(),
                ),
                "speech exercises",
            ),
            Arguments.of(
                ExerciseGroupDto(
                    id = null,
                    locale = "ru",
                    name = "Неречевые упражнения",
                    description = null,
                    series = mutableListOf(),
                ),
                "Неречевые упражнения",
            ),
            Arguments.of(
                ExerciseGroupDto(
                    id = null,
                    locale = "ru",
                    name = "Речевые упражнения",
                    description = null,
                    series = mutableListOf(),
                ),
                "Речевые упражнения",
            ),
        )
    }
}
