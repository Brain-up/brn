package com.epam.brn.upload

import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.upload.csv.CsvParser
import com.epam.brn.upload.csv.RecordProcessor
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class CsvUploadServiceTest {

    @InjectMockKs
    lateinit var uploadService: CsvUploadService

    @MockK
    lateinit var csvParser: CsvParser

    @MockK
    lateinit var recordProcessors: List<RecordProcessor<out Any, out Any>>

    @MockK
    lateinit var seriesRepository: SeriesRepository

    @MockK
    lateinit var series: Series

    @Test
    fun `should get exercise file format`() {
        // given
        every { series.type } returns ExerciseType.SINGLE_SIMPLE_WORDS.name
        every { seriesRepository.findById(1) } returns Optional.of(series)
        // when
        val actual = uploadService.getSampleStringForSeriesExerciseFile(1)
        // then
        val expected =
            """level,code,exerciseName,words,noiseLevel,noiseUrl
1,family,Семья,(сын ребёнок мама),0,
2,family,Семья,(отец брат дедушка),0,
3,family,Семья,(бабушка муж внучка),0,
4,family,Семья,(сын ребёнок родители дочь мама папа),0,
            """.trimIndent()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `should throw exception for invalid series id`() {
        val invalidSeriesId: Long = Long.MAX_VALUE
        every { seriesRepository.findById(invalidSeriesId) } returns Optional.empty()
        assertThrows(EntityNotFoundException::class.java) {
            uploadService.getSampleStringForSeriesExerciseFile(
                invalidSeriesId
            )
        }
    }
}
