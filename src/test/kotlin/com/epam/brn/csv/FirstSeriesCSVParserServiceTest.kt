package com.epam.brn.csv

import com.epam.brn.csv.converter.Converter
import com.epam.brn.csv.dto.ExerciseCsv
import com.epam.brn.csv.dto.GroupCsv
import com.epam.brn.csv.dto.SeriesCsv
import com.epam.brn.csv.dto.TaskCsv
import com.epam.brn.csv.firstSeries.TaskCSVParser1SeriesService
import com.epam.brn.csv.firstSeries.commaSeparated.CommaSeparatedExerciseCSVParserService
import com.epam.brn.csv.firstSeries.commaSeparated.CommaSeparatedGroupCSVParserService
import com.epam.brn.csv.firstSeries.commaSeparated.CommaSeparatedSeriesCSVParserService
import java.nio.charset.StandardCharsets
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FirstSeriesCSVParserServiceTest {

    private val csvMappingIteratorParser = CsvMappingIteratorParser()

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
                """.trimIndent()

        val result = input.byteInputStream(StandardCharsets.UTF_8).use {
            csvMappingIteratorParser.parseCsvFile(it, makeIdentityConverter(), taskCsvParserService)
        }.map { res -> res.value.first }.toList()

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
    fun `should parse Exercises`() {
        val input = """
                exerciseId, seriesId, level, name, description
                1, 1, 1, Однослоговые слова без шума, Однослоговые слова без шума
                2, 1, 2, Однослоговые слова без шума, Однослоговые слова без шума                
                """.trimIndent()

        val result = input.byteInputStream(StandardCharsets.UTF_8).use {
            csvMappingIteratorParser.parseCsvFile(it, makeIdentityConverter(), exerciseCsvParserService)
        }.map { res -> res.value.first }.toList()

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
                """.trimIndent()

        val result = input.byteInputStream(StandardCharsets.UTF_8).use {
            csvMappingIteratorParser.parseCsvFile(
                it,
                makeIdentityConverter(),
                groupCsvParserService
            )
        }.map { res -> res.value.first }.toList()

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
                """.trimIndent()

        val result = input.byteInputStream(StandardCharsets.UTF_8).use {
            csvMappingIteratorParser.parseCsvFile(it, makeIdentityConverter(), seriesCsvParserService)
        }.map { res -> res.value.first }.toList()

        assertThat(result).containsAll(
            listOf(
                SeriesCsv(2, 1, "Распознование слов", "Распознование слов"),
                SeriesCsv(2, 2, "Составление предложений", "Составление предложений")
            )
        )
    }
}
