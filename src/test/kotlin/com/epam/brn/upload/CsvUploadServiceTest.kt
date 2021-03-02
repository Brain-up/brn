package com.epam.brn.upload

import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.upload.csv.CsvParser
import com.epam.brn.upload.csv.RecordProcessor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
internal class CsvUploadServiceTest {

    @InjectMocks
    lateinit var uploadService: CsvUploadService

    @Mock
    lateinit var csvParser: CsvParser

    @Mock
    lateinit var recordProcessors: List<RecordProcessor<out Any, out Any>>

    @Mock
    lateinit var seriesRepository: SeriesRepository

    @Test
    fun `should get exercise file format`() {
        // given
        val series = Mockito.mock(Series::class.java)
        `when`(series.type).thenReturn(ExerciseType.SINGLE_SIMPLE_WORDS.name)
        `when`(seriesRepository.findById(1)).thenReturn(Optional.of(series))
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
        `when`(seriesRepository.findById(invalidSeriesId)).thenReturn(Optional.empty())
        assertThrows(EntityNotFoundException::class.java) {
            uploadService.getSampleStringForSeriesExerciseFile(
                invalidSeriesId
            )
        }
    }
}
