package com.epam.brn.upload.csv

import com.epam.brn.upload.csv.converter.Converter
import com.epam.brn.upload.csv.exception.CsvFileParseException
import com.epam.brn.upload.csv.iterator.impl.GroupMappingIteratorProvider
import com.epam.brn.upload.csv.iterator.impl.Series1TaskMappingIteratorProvider
import com.epam.brn.upload.csv.iterator.impl.SeriesMappingIteratorProvider
import com.epam.brn.upload.csv.record.GroupRecord
import com.epam.brn.upload.csv.record.SeriesOneTaskRecord
import com.epam.brn.upload.csv.record.SeriesRecord
import java.nio.charset.StandardCharsets
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FirstSeriesCSVParserServiceTest {

    private val parser = MappingIteratorCsvParser()

    private val taskCsvParserService = Series1TaskMappingIteratorProvider()
    private val groupCsvParserService = GroupMappingIteratorProvider()
    private val seriesCsvParserService = SeriesMappingIteratorProvider()

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
                SeriesOneTaskRecord(
                    1, "name1", 1,
                    "бал", "no_noise/бал.mp3", "pictures/бал.jpg",
                    listOf("(бам", "сам", "дам", "зал", "бак)"), "OBJECT"
                ), SeriesOneTaskRecord(
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
                GroupRecord(1, "Неречевые упражнения", "Неречевые упражнения"),
                GroupRecord(2, "Речевые упражнения", "Речевые упражнения")
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
                SeriesRecord(2, 1, "Распознование слов", "Распознование слов"),
                SeriesRecord(2, 2, "Составление предложений", "Составление предложений")
            )
        )
    }
}
