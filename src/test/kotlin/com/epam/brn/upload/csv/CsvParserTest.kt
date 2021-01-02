package com.epam.brn.upload.csv

import com.epam.brn.model.ExerciseType
import com.epam.brn.upload.csv.group.GroupRecord
import com.epam.brn.upload.csv.group.GroupRecordMappingIteratorProvider
import com.epam.brn.upload.csv.nonspeech.SignalSeriesRecord
import com.epam.brn.upload.csv.nonspeech.SignalSeriesRecordProvider
import com.epam.brn.upload.csv.series.SeriesGenericRecord
import com.epam.brn.upload.csv.series.SeriesGenericRecordMappingIteratorProvider
import com.epam.brn.upload.csv.series1.SeriesOneRecord
import com.epam.brn.upload.csv.series1.SeriesOneRecordMappingIteratorProvider
import com.epam.brn.upload.csv.series2.SeriesTwoRecordMappingIteratorProvider
import com.epam.brn.upload.csv.series3.SeriesThreeRecord
import com.epam.brn.upload.csv.series3.SeriesThreeRecordMappingIteratorProvider
import com.epam.brn.upload.csv.series4.SeriesFourRecord
import com.epam.brn.upload.csv.series4.SeriesFourRecordMappingIteratorProvider
import com.epam.brn.upload.csv.subgroup.SubGroupGenericRecordMappingIteratorProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.charset.StandardCharsets

class CsvParserTest {

    private val parser = CsvParser(
        listOf(
            GroupRecordMappingIteratorProvider(),
            SeriesGenericRecordMappingIteratorProvider(),
            SubGroupGenericRecordMappingIteratorProvider(),
            SeriesOneRecordMappingIteratorProvider(),
            SeriesTwoRecordMappingIteratorProvider(),
            SeriesThreeRecordMappingIteratorProvider(),
            SeriesFourRecordMappingIteratorProvider(),
            SignalSeriesRecordProvider()
        )
    )

    @Test
    fun `should parse Groups`() {

        val input = """
                groupId, locale, name, description
                1, ru, Неречевые упражнения, Неречевые упражнения
                2, ru, Речевые упражнения, Речевые упражнения              
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
                GroupRecord(
                    1,
                    "ru",
                    "Неречевые упражнения",
                    "Неречевые упражнения"
                ),
                GroupRecord(
                    2,
                    "ru",
                    "Речевые упражнения",
                    "Речевые упражнения"
                )
            )
        )
    }

    @Test
    fun `should parse Series`() {
        val input = """
                groupId, level, type, name, description
                1, 2, type, Составление предложений, Это составление предложений         
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
                SeriesGenericRecord(
                    1,
                    2,
                    "type",
                    "Составление предложений",
                    "Это составление предложений"
                )
            )
        )
    }

    @Test
    fun `should parse exercises for Series 1`() {
        val input = """
                level,code,exerciseName,words,noiseLevel,noiseUrl
                1,family,Семья,(сын ребёнок мама),0,
                2,family,Семья,(отец брат дедушка),0,
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
                SeriesOneRecord(
                    1, "family",
                    "Семья",
                    mutableListOf("(сын", "ребёнок", "мама)"),
                    0,
                    ""
                ),
                SeriesOneRecord(
                    2, "family",
                    "Семья",
                    mutableListOf("(отец", "брат", "дедушка)"),
                    0,
                    ""
                )
            )
        )
    }

    @Test
    fun `should parse exercises for Series 3`() {
        val input = """
                level,code,exerciseName,words,answerAudioFile,answerParts
                1,sentence_2,Пойми предложение из 2 слов,(();();(девочка дедушка бабушка); (бросает читает рисует);();()),series3/девочка_бросает.mp3,(девочка бросает)
                2,sentence_2,Пойми предложение из 2 слов,(();();(девочка дедушка бабушка); (бросает читает рисует);();()),series3/девочка_читает.mp3,(девочка читает)
                3,sentence_2,Пойми предложение из 2 слов,(();();(девочка дедушка бабушка); (бросает читает рисует);();()),series3/девочка_рисует.mp3,(девочка рисует)
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
                SeriesThreeRecord(
                    1,
                    "Пойми предложение из 2 слов",
                    "sentence_2",
                    mutableListOf("(()", "()", "(девочка дедушка бабушка)", "(бросает читает рисует)", "()", "())"),
                    "series3/девочка_бросает.mp3",
                    "(девочка бросает)"
                ),
                SeriesThreeRecord(
                    2,
                    "Пойми предложение из 2 слов",
                    "sentence_2",
                    mutableListOf("(()", "()", "(девочка дедушка бабушка)", "(бросает читает рисует)", "()", "())"),
                    "series3/девочка_читает.mp3",
                    "(девочка читает)"
                ),
                SeriesThreeRecord(
                    3,
                    "Пойми предложение из 2 слов",
                    "sentence_2",
                    mutableListOf("(()", "()", "(девочка дедушка бабушка)", "(бросает читает рисует)", "()", "())"),
                    "series3/девочка_рисует.mp3",
                    "(девочка рисует)"
                )
            )
        )
    }

    @Test
    fun `should parse exercises for Series 4 Phrases`() {
        val input = """
                level,code,exerciseName,phrases,noiseLevel,noiseUrl
                1,longShortPhrases,Фразы разной длительности,(Мамочка идёт. Мамочка быстро идёт в магазин.),0,
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)
        val result = parser.parse(input)
        assertThat(result).containsAll(
            listOf(
                SeriesFourRecord(
                    1,
                    "longShortPhrases",
                    "Фразы разной длительности",
                    mutableListOf("(Мамочка", "идёт.", "Мамочка", "быстро", "идёт", "в", "магазин.)"),
                    0,
                    ""
                )
            )
        )
    }

    @Test
    fun `should parse exercise for non speech Series`() {
        val input = """
                level,code,exerciseName,exerciseType,signals
                1,durationSignals,По 2 сигнала разной длительности,DURATION_SIGNALS,1000 60; 1000 120
                1,frequencySignals,По 2 сигнала разной частоты,FREQUENCY_SIGNALS,500 120; 1500 120
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        val result = parser.parse(input)

        assertThat(result).containsAll(
            listOf(
                SignalSeriesRecord(
                    code = "durationSignals",
                    level = 1,
                    exerciseName = "По 2 сигнала разной длительности",
                    exerciseType = ExerciseType.DURATION_SIGNALS,
                    signals = listOf("1000 60", "1000 120")
                ),
                SignalSeriesRecord(
                    code = "frequencySignals",
                    level = 1,
                    exerciseName = "По 2 сигнала разной частоты",
                    exerciseType = ExerciseType.FREQUENCY_SIGNALS,
                    signals = listOf("500 120", "1500 120")
                )
            )
        )
    }

    @Test
    fun `should throw parse exception`() {
        val input = """
                level pictureUrl exerciseName orderNumber word audioFileName pictureFileName words wordType
                incorrect string
                """.trimIndent().byteInputStream(StandardCharsets.UTF_8)

        assertThrows<CsvParser.ParseException> {
            parser.parse(input)
        }
    }
}
