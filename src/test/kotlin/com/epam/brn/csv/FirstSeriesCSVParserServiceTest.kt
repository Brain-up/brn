package com.epam.brn.csv

import com.epam.brn.csv.converter.Converter
import com.epam.brn.csv.converter.impl.firstSeries.ExerciseCsvConverter
import com.epam.brn.csv.converter.impl.firstSeries.GroupCsvConverter
import com.epam.brn.csv.converter.impl.firstSeries.SeriesCsvConverter
import com.epam.brn.csv.converter.impl.firstSeries.TaskCsv1SeriesConverter
import com.epam.brn.csv.dto.ExerciseCsv
import com.epam.brn.csv.dto.GroupCsv
import com.epam.brn.csv.dto.SeriesCsv
import com.epam.brn.csv.dto.TaskCsv
import com.fasterxml.jackson.databind.MappingIterator
import java.io.InputStream
import java.nio.charset.StandardCharsets
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

private inline fun <reified T> makeIdentityConverter(noinline iteratorProvider: (InputStream) -> MappingIterator<T>): Converter<T, T> =
    object : Converter<T, T> {
        override fun convert(source: T) = source
        override fun iteratorProvider(): (file: InputStream) -> MappingIterator<T> = iteratorProvider
    }

val csvMappintIteratorParser = CsvMappingIteratorParser()

class FirstSeriesCSVParserServiceTest : Spek({
    describe("Csv Parser Service") {
        it("should parse Tasks") {

            val input = """
                level exerciseName orderNumber word audioFileName pictureFileName words wordType
                1 name1 1 бал no_noise/бал.mp3 pictures/бал.jpg (бам,сам,дам,зал,бак) OBJECT
                2 name1 3 foo no_noise/foo.mp3 pictures/foo.jpg (foo,bar,baz) OBJECT
                """.trimIndent()

            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                csvMappintIteratorParser.parseCsvFile(it, makeIdentityConverter(TaskCsv1SeriesConverter().iteratorProvider()))
            }.map { res -> res.value.first }.toList()

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

            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                csvMappintIteratorParser.parseCsvFile(it, makeIdentityConverter<ExerciseCsv>(ExerciseCsvConverter().iteratorProvider()))
            }.map { res -> res.value.first }.toList()

            val name = "Однослоговые слова без шума"
            result shouldBeEqualTo listOf(
                ExerciseCsv(1, 1, 1, name, name), ExerciseCsv(2, 1, 2, name, name)
            )
        }

        it("should parse Groups") {

            val input = """
                groupId, name, description
                1, Неречевые упражнения, Неречевые упражнения
                2, Речевые упражнения, Речевые упражнения              
                """.trimIndent()

            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                csvMappintIteratorParser.parseCsvFile(it, makeIdentityConverter<GroupCsv>(GroupCsvConverter().iteratorProvider()))
            }.map { res -> res.value.first }.toList()

            result shouldContain GroupCsv(1, "Неречевые упражнения", "Неречевые упражнения")
            result shouldContain GroupCsv(2, "Речевые упражнения", "Речевые упражнения")
        }

        it("should parse Series") {

            val input = """
                groupId, seriesId, name, description
                2, 1, Распознование слов, Распознование слов
                2, 2, Составление предложений, Составление предложений         
                """.trimIndent()

            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                csvMappintIteratorParser.parseCsvFile(it, makeIdentityConverter<SeriesCsv>(SeriesCsvConverter().iteratorProvider()))
            }.map { res -> res.value.first }.toList()

            result shouldContain SeriesCsv(2, 1, "Распознование слов", "Распознование слов")
            result shouldContain SeriesCsv(2, 2, "Составление предложений", "Составление предложений")
        }
    }
})
