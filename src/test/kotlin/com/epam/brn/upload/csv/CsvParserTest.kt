package com.epam.brn.upload.csv

import com.epam.brn.upload.csv.group.GroupRecord
import com.epam.brn.upload.csv.group.GroupRecordMappingIteratorProvider
import com.epam.brn.upload.csv.series.SeriesGenericRecord
import com.epam.brn.upload.csv.series.SeriesGenericRecordMappingIteratorProvider
import com.epam.brn.upload.csv.series1.SeriesOneRecord
import com.epam.brn.upload.csv.series1.SeriesOneRecordMappingIteratorProvider
import com.epam.brn.upload.csv.series2.SeriesTwoRecordMappingIteratorProvider
import com.epam.brn.upload.csv.series3.SeriesThreeRecord
import com.epam.brn.upload.csv.series3.SeriesThreeRecordMappingIteratorProvider
import com.epam.brn.upload.csv.series4.SeriesFourRecord
import com.epam.brn.upload.csv.series4.SeriesFourRecordMappingIteratorProvider
import java.nio.charset.StandardCharsets
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CsvParserTest {

    private val parser = CsvParser(
        listOf(
            GroupRecordMappingIteratorProvider(),
            SeriesGenericRecordMappingIteratorProvider(),
            SeriesOneRecordMappingIteratorProvider(),
            SeriesTwoRecordMappingIteratorProvider(),
            SeriesThreeRecordMappingIteratorProvider(),
            SeriesFourRecordMappingIteratorProvider()
        )
    )

    @Test
    fun `should parse Tasks`() {
        val input = """
                level exerciseName orderNumber word audioFileName pictureFileName words wordType
                1 name1 1 бал no_noise/бал.mp3 pictures/бал.jpg (бам,сам,дам,зал,бак) OBJECT
                2 name1 3 foo no_noise/foo.mp3 pictures/foo.jpg (foo,bar,baz) OBJECT
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
                SeriesOneRecord(
                    1, "name1", 1,
                    "бал", "no_noise/бал.mp3", "pictures/бал.jpg",
                    listOf("(бам", "сам", "дам", "зал", "бак)"), "OBJECT"
                ), SeriesOneRecord(
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

        assertThrows<CsvParser.ParseException> {
            parser.parse(input)
        }
    }

    @Test
    fun `should throw exception with parse errors`() {
        val input = """
                level exerciseName orderNumber word audioFileName pictureFileName words wordType
                incorrect string 1
                incorrect string 2
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val actual = assertThrows<CsvParser.ParseException> {
            parser.parse(input)
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

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
                GroupRecord(
                    1,
                    "Неречевые упражнения",
                    "Неречевые упражнения"
                ),
                GroupRecord(
                    2,
                    "Речевые упражнения",
                    "Речевые упражнения"
                )
            )
        )
    }

    @Test
    fun `should parse Series`() {
        val input = """
                groupId, seriesId, name, description
                2, 1, Распознавание слов, Распознавание слов
                2, 2, Составление предложений, Составление предложений         
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
                SeriesGenericRecord(
                    2,
                    1,
                    "Распознавание слов",
                    "Распознавание слов"
                ),
                SeriesGenericRecord(
                    2,
                    2,
                    "Составление предложений",
                    "Составление предложений"
                )
            )
        )
    }

    @Test
    fun `should parse exercise for Series 3`() {
        val input = """
                level,exerciseName,orderNumber,words,answerAudioFile,answerParts
                1,Распознавание предложений из 2 слов,1,(();();(девочка дедушка бабушка); (бросает читает рисует);();()),series3/девочка_бросает.mp3,(девочка бросает)
                1,Распознавание предложений из 2 слов,2,(();();(девочка дедушка бабушка); (бросает читает рисует);();()),series3/девочка_читает.mp3,(девочка читает)
                1,Распознавание предложений из 2 слов,3,(();();(девочка дедушка бабушка); (бросает читает рисует);();()),series3/девочка_рисует.mp3,(девочка рисует)
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
                SeriesThreeRecord(
                    1,
                    "Распознавание предложений из 2 слов",
                    1,
                    mutableListOf("(()", "()", "(девочка дедушка бабушка)", "(бросает читает рисует)", "()", "())"),
                    "series3/девочка_бросает.mp3",
                    "(девочка бросает)"
                ),
                SeriesThreeRecord(
                    1,
                    "Распознавание предложений из 2 слов",
                    2,
                    mutableListOf("(()", "()", "(девочка дедушка бабушка)", "(бросает читает рисует)", "()", "())"),
                    "series3/девочка_читает.mp3",
                    "(девочка читает)"
                ),
                SeriesThreeRecord(
                    1,
                    "Распознавание предложений из 2 слов",
                    3,
                    mutableListOf("(()", "()", "(девочка дедушка бабушка)", "(бросает читает рисует)", "()", "())"),
                    "series3/девочка_рисует.mp3",
                    "(девочка рисует)"
                )
            )
        )
    }

    @Test
    fun `should parse exercise for Series 4`() {
        val input = """
                level,exerciseName,words,noise
                1,Слова без шума,(бал бум быль вить гад дуб),no_noise
                2,Слова без шума,(линь лис моль пар пять раб),no_noise
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
                SeriesFourRecord(
                    1,
                    "Слова без шума",
                    mutableListOf("(бал", "бум", "быль", "вить", "гад", "дуб)"),
                    "no_noise"
                ),
                SeriesFourRecord(
                    2,
                    "Слова без шума",
                    mutableListOf("(линь", "лис", "моль", "пар", "пять", "раб)"),
                    "no_noise"
                )
            )
        )
    }
}
