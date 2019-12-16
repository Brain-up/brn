package com.epam.brn.service.parsers.csv

import com.epam.brn.service.parsers.csv.converter.Converter
import com.epam.brn.service.parsers.csv.dto.ExerciseCsv
import com.epam.brn.service.parsers.csv.dto.GroupCsv
import com.epam.brn.service.parsers.csv.dto.SeriesCsv
import com.epam.brn.service.parsers.csv.dto.TaskCsv
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.nio.charset.StandardCharsets

private inline fun <reified T> makeIdentityConverter(): Converter<T, T> =
    object : Converter<T, T> {
        override fun convert(source: T) = source
    }

val csvParserService = CSVParserService()

class CSVParserServiceTest : Spek({
    describe("Csv Parser Service") {
        it("should parse Tasks") {

            val input = """
                exerciseId orderNumber word audioFileName pictureFileName words wordType
                1 1 бал no_noise/бал.mp3 pictures/бал.jpg (бам,сам,дам,зал,бак) OBJECT
                1 3 foo no_noise/foo.mp3 pictures/foo.jpg (foo,bar,baz) OBJECT
                """.trimIndent()

            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                csvParserService.parseCsvFile(it, makeIdentityConverter<TaskCsv>())
            }

            result shouldEqual listOf(
                TaskCsv(
                    1, 1, "бал", "no_noise/бал.mp3", "pictures/бал.jpg",
                    listOf("(бам", "сам", "дам", "зал", "бак)") , "OBJECT"
                ),
                TaskCsv(
                    1, 3, "foo", "no_noise/foo.mp3", "pictures/foo.jpg",
                    listOf("(foo", "bar", "baz)"), "OBJECT"
                )
            )
        }

        it("should parse Exercises") {

            val input = """
                exerciseId, seriesId, level, name, description
                1, 1, 1, Однослоговые слова без шума, Однослоговые слова без шума
                2, 1, 2, Однослоговые слова без шума, Однослоговые слова без шума                
                """.trimIndent()

            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                csvParserService.parseCommasSeparatedCsvFile(it, makeIdentityConverter<ExerciseCsv>())
            }

            val name = "Однослоговые слова без шума"
            result shouldEqual listOf(
                ExerciseCsv(1, 1, 1, name, name), ExerciseCsv(2, 1, 2, name, name)
            )
        }

        it("should parse Groups") {

            val input = """
                groupId,  name,  description
                1, Неречевые упражнения, Неречевые упражнения
                2, Речевые упражнения, Речевые упражнения              
                """.trimIndent()

            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                csvParserService.parseCommasSeparatedCsvFile(it, makeIdentityConverter<GroupCsv>())
            }

            result shouldEqual listOf(
                GroupCsv(1, "Неречевые упражнения", "Неречевые упражнения"),
                GroupCsv(
                    2, "Речевые упражнения", "Речевые упражнения"
                )
            )
        }

        it("should parse Series") {

            val input = """
                groupId, seriesId, name, description
                2, 1, Распознование слов, Распознование слов
                2, 2, Составление предложений, Составление предложений         
                """.trimIndent()

            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                csvParserService.parseCommasSeparatedCsvFile(it, makeIdentityConverter<SeriesCsv>())
            }

            result shouldEqual listOf(
                SeriesCsv(2, 1, "Распознование слов", "Распознование слов"),
                SeriesCsv(
                    2, 2, "Составление предложений", "Составление предложений"
                )
            )
        }
    }
})