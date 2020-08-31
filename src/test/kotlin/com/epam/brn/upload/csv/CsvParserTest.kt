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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.charset.StandardCharsets

class CsvParserTest {

    private val parser = CsvParser(
        listOf(
            GroupRecordMappingIteratorProvider(),
            SeriesGenericRecordMappingIteratorProvider(),
            SeriesTwoRecordMappingIteratorProvider(),
            SeriesThreeRecordMappingIteratorProvider(),
            SeriesOneRecordMappingIteratorProvider()
        )
    )

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
                2, 2, Составление предложений, Составление предложений         
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
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
    fun `should parse exercise for Series 1`() {
        val input = """
                level,exerciseName,words,noiseLevel,noiseUrl
                1,Слова без шума,(бал бум быль вить гад дуб),0,
                2,Слова без шума,(линь лис моль пар пять раб),0,
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
                SeriesOneRecord(
                    1,
                    "Слова без шума",
                    mutableListOf("(бал", "бум", "быль", "вить", "гад", "дуб)"),
                    0,
                    ""
                ),
                SeriesOneRecord(
                    2,
                    "Слова без шума",
                    mutableListOf("(линь", "лис", "моль", "пар", "пять", "раб)"),
                    0,
                    ""
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
}
