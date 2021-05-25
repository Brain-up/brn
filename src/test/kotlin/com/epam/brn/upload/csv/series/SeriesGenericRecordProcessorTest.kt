package com.epam.brn.upload.csv.series

import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.service.ExerciseGroupsService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito

@ExtendWith(MockKExtension::class)
internal class SeriesGenericRecordProcessorTest {

    @InjectMockKs
    private lateinit var seriesGenericRecordProcessor: SeriesGenericRecordProcessor

    @MockK
    private lateinit var exerciseGroupsService: ExerciseGroupsService

    @MockK
    private lateinit var seriesRepository: SeriesRepository

    private val exerciseGroups = listOf(
        ExerciseGroup(
            code = "CODE1",
            name = "name1",
            locale = "ru-ru1",
            description = "desc1"
        ),
        ExerciseGroup(
            code = "CODE2",
            name = "name2",
            locale = "ru-ru2",
            description = "desc2"
        )
    )

    @Test
    fun `should create correct series`() {
        // GIVEN
        val records = createInputSeriesGenericRecordList()
        val expected = createActualSeriesList()
        val seriesMock = Mockito.mock(Series::class.java)
        every { exerciseGroupsService.findGroupByCode(ofType(String::class)) } returnsMany exerciseGroups
        for (i in exerciseGroups.indices) {
            every { exerciseGroupsService.findGroupByCode(match { it == exerciseGroups[i].code }) } returns exerciseGroups[i]
        }
        every { seriesRepository.findByTypeAndName(ofType(String::class), ofType(String::class)) } returns null
        every { seriesRepository.save(ofType(Series::class)) } returns seriesMock
        // WHEN
        val actual = seriesGenericRecordProcessor.process(records)
        // THEN
        for (i in expected.indices) {
            Assertions.assertThat(actual[i]).isEqualTo(expected[i])
        }
        verify(exactly = records.size) { exerciseGroupsService.findGroupByCode(ofType(String::class)) }
        verify(exactly = records.size) {
            seriesRepository.findByTypeAndName(
                ofType(String::class),
                ofType(String::class)
            )
        }
        verify(exactly = records.size) { seriesRepository.save(ofType(Series::class)) }
    }

    @Test
    fun `should not call save series when all records exists in DB`() {
        // GIVEN
        val records = createInputSeriesGenericRecordList()
        val expected = createActualSeriesList()
        val seriesMock = Mockito.mock(Series::class.java)
        every { exerciseGroupsService.findGroupByCode(ofType(String::class)) } returnsMany exerciseGroups
        for (i in exerciseGroups.indices) {
            every { exerciseGroupsService.findGroupByCode(match { it == exerciseGroups[i].code }) } returns exerciseGroups[i]
        }
        every { seriesRepository.findByTypeAndName(ofType(String::class), ofType(String::class)) } returns seriesMock
        every { seriesRepository.save(ofType(Series::class)) } returns seriesMock
        // WHEN
        val actual = seriesGenericRecordProcessor.process(records)
        // THEN
        for (i in expected.indices) {
            Assertions.assertThat(actual[i]).isEqualTo(expected[i])
        }
        verify(exactly = records.size) { exerciseGroupsService.findGroupByCode(ofType(String::class)) }
        verify(exactly = records.size) {
            seriesRepository.findByTypeAndName(
                ofType(String::class),
                ofType(String::class)
            )
        }
        verify(inverse = true) { seriesRepository.save(ofType(Series::class)) }
    }

    private fun createActualSeriesList() = mutableListOf(
        Series(
            type = "WORDS_SEQUENCES",
            name = "name",
            level = 0,
            description = "desc",
            exerciseGroup = exerciseGroups[0]
        ),
        Series(
            type = "SENTENCE",
            name = "name1",
            level = 1,
            description = "desc1",
            exerciseGroup = exerciseGroups[0]
        ),
        Series(
            type = "SINGLE_SIMPLE_WORDS",
            name = "name2",
            level = 2,
            description = "desc2",
            exerciseGroup = exerciseGroups[1]
        )
    )

    private fun createInputSeriesGenericRecordList() = mutableListOf(
        SeriesGenericRecord(
            groupCode = "CODE1",
            level = 0,
            type = "WORDS_SEQUENCES",
            name = "name",
            description = "desc"
        ),
        SeriesGenericRecord(
            groupCode = "CODE1",
            level = 1,
            type = "SENTENCE",
            name = "name1",
            description = "desc1"
        ),
        SeriesGenericRecord(
            groupCode = "CODE2",
            level = 2,
            type = "SINGLE_SIMPLE_WORDS",
            name = "name2",
            description = "desc2"
        )
    )
}
