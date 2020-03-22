package com.epam.brn.csv

import com.epam.brn.csv.converter.Converter
import com.epam.brn.csv.dto.ExerciseCsv
import com.epam.brn.csv.dto.GroupCsv
import com.epam.brn.csv.dto.SeriesCsv
import com.epam.brn.csv.dto.TaskCsv
import com.epam.brn.csv.exception.CsvFileParseException
import com.epam.brn.csv.firstSeries.TaskCSVParser1SeriesService
import com.epam.brn.csv.firstSeries.commaSeparated.CommaSeparatedExerciseCSVParserService
import com.epam.brn.csv.firstSeries.commaSeparated.CommaSeparatedGroupCSVParserService
import com.epam.brn.csv.firstSeries.commaSeparated.CommaSeparatedSeriesCSVParserService
import java.nio.charset.StandardCharsets
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FirstSeriesCSVParserServiceTest {

    private val parser = CsvMappingIteratorParser()

    private val taskCsvParserService = TaskCSVParser1SeriesService()
    private val exerciseCsvParserService = CommaSeparatedExerciseCSVParserService()
    private val groupCsvParserService = CommaSeparatedGroupCSVParserService()
    private val seriesCsvParserService = CommaSeparatedSeriesCSVParserService()

    private inline fun <reified T> makeIdentityConverter(): Converter<T, T> {
        return object : Converter<T, T> {
            override fun convert(source: T) = source
        }
    }

    @Test
    fun `should parse Tasks`() {
        val input = """
                level exerciseName orderNumber word audioFileName pictureFileName words wordType
                1 name1 1 бал no_noise/бал.mp3 pictures/бал.jpg (бам,сам,дам,зал,бак) OBJECT
                2 name1 3 foo no_noise/foo.mp3 pictures/foo.jpg (foo,bar,baz) OBJECT
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input, makeIdentityConverter(), taskCsvParserService)

        assertThat(result).containsAll(
            listOf(
                TaskCsv(
                    1, "name1", 1,
                    "бал", "no_noise/бал.mp3", "pictures/бал.jpg",
                    listOf("(бам", "сам", "дам", "зал", "бак)"), "OBJECT"
                ), TaskCsv(
                    2, "name1", 3,
                    "foo", "no_noise/foo.mp3", "pictures/foo.jpg",
                    listOf("(foo", "bar", "baz)"), "OBJECT"
                )
            )
        )
    }

    @Test
    fun `should throw parse exception`() {
        val input = """
                level exerciseName orderNumber word audioFileName pictureFileName words wordType
                incorrect string
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        assertThrows<CsvFileParseException> {
            parser.parse(input, makeIdentityConverter(), taskCsvParserService)
        }
    }

    @Test
    fun `should throw exception with parse errors`() {
        val input = """
                level exerciseName orderNumber word audioFileName pictureFileName words wordType
                incorrect string 1
                incorrect string 2
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val actual = assertThrows<CsvFileParseException> {
            parser.parse(input, makeIdentityConverter(), taskCsvParserService)
        }.errors

        assertThat(actual[0]).startsWith("Failed to parse line 2: 'incorrect string 1'. Error: ")
        assertThat(actual[1]).startsWith("Failed to parse line 3: 'incorrect string 2'. Error: ")
    }

    @Test
    fun `should parse Exercises`() {
        val input = """
                exerciseId, seriesId, level, name, description
                1, 1, 1, Однослоговые слова без шума, Однослоговые слова без шума
                2, 1, 2, Однослоговые слова без шума, Однослоговые слова без шума                
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input, makeIdentityConverter(), exerciseCsvParserService)

        val name = "Однослоговые слова без шума"
        assertThat(result).containsAll(
            listOf(
                ExerciseCsv(1, 1, 1, name, name),
                ExerciseCsv(2, 1, 2, name, name)
            )
        )
    }

    @Test
    fun `should parse Groups`() {

        val input = """
                groupId, name, description
                1, Неречевые упражнения, Неречевые упражнения
                2, Речевые упражнения, Речевые упражнения              
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser
            .parse(input, makeIdentityConverter(), groupCsvParserService)

        assertThat(result).containsAll(
            listOf(
                GroupCsv(1, "Неречевые упражнения", "Неречевые упражнения"),
                GroupCsv(2, "Речевые упражнения", "Речевые упражнения")
            )
        )
    }

    @Test
    fun `should parse Series`() {
        val input = """
                groupId, seriesId, name, description
                2, 1, Распознование слов, Распознование слов
                2, 2, Составление предложений, Составление предложений         
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input, makeIdentityConverter(), seriesCsvParserService)

        assertThat(result).containsAll(
            listOf(
                SeriesCsv(2, 1, "Распознование слов", "Распознование слов"),
                SeriesCsv(2, 2, "Составление предложений", "Составление предложений")
            )
        )
    }
}
