package com.epam.brn.upload

import com.epam.brn.upload.csv.CsvParser
import com.epam.brn.upload.csv.RecordProcessor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.io.IOException

@ExtendWith(MockitoExtension::class)
internal class CsvUploadServiceTest {

    @InjectMocks
    lateinit var uploadService: CsvUploadService

    @Mock
    lateinit var csvParser: CsvParser

    @Mock
    lateinit var recordProcessors: List<RecordProcessor<out Any, out Any>>

    @Test
    fun `should get exercise file format`() {
        val expected = """level,exerciseName,words,noise
1,Семья,(сын ребенок родители дочь мама папа),0
2,Семья,(отец мать сестра брат дядя дедушка),0
3,Семья,(бабушка муж жена внучка внук внуки),0
4,Семья,(семья тётя дядя племянник племянница родня),0""".trimIndent()

        val actual = uploadService.getSampleStringForSeriesExerciseFile(1)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `should throw exception for missing series file`() {
        val invalidSeriesId: Long = Long.MAX_VALUE

        assertThrows(IOException::class.java) { uploadService.getSampleStringForSeriesExerciseFile(invalidSeriesId) }
    }
}
