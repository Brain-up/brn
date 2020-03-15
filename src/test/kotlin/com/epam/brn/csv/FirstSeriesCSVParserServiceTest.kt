package com.epam.brn.csv

import com.epam.brn.csv.converter.impl.DefaultEntityConverter
import com.epam.brn.csv.converter.impl.ObjectReaderService
import com.epam.brn.csv.converter.impl.StreamToStringMapper
import com.epam.brn.csv.dto.ExerciseCsv
import com.epam.brn.csv.dto.GroupCsv
import com.epam.brn.csv.dto.SeriesCsv
import com.epam.brn.csv.dto.TaskCsv
import java.nio.charset.StandardCharsets
import kotlin.streams.toList
import org.amshove.kluent.shouldContain
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class FirstSeriesCSVParserServiceTest : Spek({
    describe("Csv Parser Service") {
        it("should parse Tasks") {

            val input = """
                level exerciseName orderNumber word audioFileName pictureFileName words wordType
                1 name1 1 бал no_noise/бал.mp3 pictures/бал.jpg (бам,сам,дам,зал,бак) OBJECT
                2 name1 3 foo no_noise/foo.mp3 pictures/foo.jpg (foo,bar,baz) OBJECT
                """.trimIndent()

            fun defaultEntityConverter() = DefaultEntityConverter()
            fun streamToStringMapper() = StreamToStringMapper()
            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                defaultEntityConverter().parseCsvFile<TaskCsv>(
                    streamToStringMapper().getCsvLineNumbersToValues(it),
                    ObjectReaderService().seriesOneReader().objectReader<TaskCsv>().readValues(it)
                )
            }.map { res -> res.data.get() }.toList()

            result shouldContain TaskCsv(
                1, "name1", 1, "бал", "no_noise/бал.mp3", "pictures/бал.jpg",
                listOf("(бам", "сам", "дам", "зал", "бак)"), "OBJECT"
            )

            result shouldContain TaskCsv(
                2, "name1", 3, "foo", "no_noise/foo.mp3", "pictures/foo.jpg",
                listOf("(foo", "bar", "baz)"), "OBJECT"
            )
        }

        it("should parse Exercises") {

            val input = """
                exerciseId, seriesId, level, name, description
                1, 1, 1, Однослоговые слова без шума, Однослоговые слова без шума
                2, 1, 2, Однослоговые слова без шума, Однослоговые слова без шума
                """.trimIndent()

            fun defaultEntityConverter() = DefaultEntityConverter()
            fun streamToStringMapper() = StreamToStringMapper()
            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                defaultEntityConverter().parseCsvFile<ExerciseCsv>(
                    streamToStringMapper().getCsvLineNumbersToValues(it),
                    ObjectReaderService().exerciseReader().objectReader<ExerciseCsv>().readValues(it)
                )
            }.map { res -> res.data.get() }.toList()

            val name = "Однослоговые слова без шума"
            result shouldContain ExerciseCsv(1, 1, 1, name, name)
            result shouldContain ExerciseCsv(2, 1, 2, name, name)
        }

        it("should parse Groups") {

            val input = """
                groupId, name, description
                1, Неречевые упражнения, Неречевые упражнения
                2, Речевые упражнения, Речевые упражнения
                """.trimIndent()

            fun defaultEntityConverter() = DefaultEntityConverter()
            fun streamToStringMapper() = StreamToStringMapper()
            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                defaultEntityConverter().parseCsvFile<GroupCsv>(
                    streamToStringMapper().getCsvLineNumbersToValues(it),
                    ObjectReaderService().groupReader().objectReader<GroupCsv>().readValues(it)
                )
            }.map { res -> res.data.get() }.toList()

            result shouldContain GroupCsv(1, "Неречевые упражнения", "Неречевые упражнения")
            result shouldContain GroupCsv(2, "Речевые упражнения", "Речевые упражнения")
        }

        it("should parse Series") {

            val input = """
                groupId, seriesId, name, description
                2, 1, Распознование слов, Распознование слов
                2, 2, Составление предложений, Составление предложений
                """.trimIndent()

            fun defaultEntityConverter() = DefaultEntityConverter()
            fun streamToStringMapper() = StreamToStringMapper()
            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                defaultEntityConverter().parseCsvFile<SeriesCsv>(
                    streamToStringMapper().getCsvLineNumbersToValues(it),
                    ObjectReaderService().seriesReader().objectReader<SeriesCsv>().readValues(it)
                )
            }.map { res -> res.data.get() }.toList()

            result shouldContain SeriesCsv(2, 1, "Распознование слов", "Распознование слов")
            result shouldContain SeriesCsv(2, 2, "Составление предложений", "Составление предложений")
        }
    }
})
