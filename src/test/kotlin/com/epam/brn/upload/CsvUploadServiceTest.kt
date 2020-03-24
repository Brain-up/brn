package com.epam.brn.upload

import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.upload.csv.parser.CsvParser
import java.io.IOException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class CsvUploadServiceTest {

    @InjectMocks
    lateinit var uploadService: CsvUploadService

    @Mock
    lateinit var csvParser: CsvParser

    @Mock
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Mock
    lateinit var seriesRepository: SeriesRepository

    @Mock
    lateinit var exerciseRepository: ExerciseRepository

    @Mock
    lateinit var taskRepository: TaskRepository

    @Test
    fun `should get series file format`() {
        val expected = """
            level exerciseName orderNumber word audioFileName pictureFileName words wordType
            1 "Однослоговые слова без шума" 1 бал no_noise/бал.mp3 pictures/бал.jpg (бам,сам,дам,зал,бум) OBJECT
            1 "Однослоговые слова без шума" 2 бум no_noise/бум.mp3 pictures/бум.jpg (зум,кум,шум,зуб,куб) OBJECT
            1 "Однослоговые слова без шума" 3 быль no_noise/быль.mp3 pictures/быль.jpg (пыль,соль,мыль,дыль,киль) OBJECT
            1 "Однослоговые слова без шума" 4 вить no_noise/вить.mp3 pictures/вить.jpg (бить,жить,мыль,выть,лить) OBJECT_ACTION
            """.trimIndent()

        val actual = uploadService.getSampleStringForSeriesFile(1)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `should throw exception for missing series file`() {
        val invalidSeriesId: Long = Long.MAX_VALUE

        assertThrows(IOException::class.java) { uploadService.getSampleStringForSeriesFile(invalidSeriesId) }
    }
}
