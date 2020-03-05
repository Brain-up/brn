package com.epam.brn.csv

import com.epam.brn.csv.converter.impl.DefaultEntityConverter
import com.epam.brn.csv.converter.impl.firstSeries.ExerciseUploader
import com.epam.brn.csv.converter.impl.firstSeries.GroupUploader
import com.epam.brn.csv.converter.impl.firstSeries.SeriesOneUploader
import com.epam.brn.csv.converter.impl.firstSeries.SeriesUploader
import com.epam.brn.csv.dto.ExerciseCsv
import com.epam.brn.csv.dto.GroupCsv
import com.epam.brn.csv.dto.SeriesCsv
import com.epam.brn.csv.dto.TaskCsv
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.service.ExerciseGroupsService
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.SeriesService
import com.epam.brn.service.TaskService
import java.nio.charset.StandardCharsets
import org.amshove.kluent.shouldContain
import org.mockito.Mockito
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
            val exerciseService = Mockito.mock(ExerciseService::class.java)
            val seriesService = Mockito.mock(SeriesService::class.java)
            val taskService = Mockito.mock(TaskService::class.java)
            val resourceService = Mockito.mock(ResourceService::class.java)
            fun task1Converter() = SeriesOneUploader(exerciseService, seriesService, taskService, resourceService, "")
            fun defaultEntityConverter() = DefaultEntityConverter()
            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                defaultEntityConverter().parseCsvFile<TaskCsv>(it, task1Converter())
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
            var exerciseRepository = Mockito.mock(ExerciseRepository::class.java)
            var seriesService = Mockito.mock(SeriesService::class.java)
            fun exerciseCsvConverter() = ExerciseUploader(exerciseRepository, seriesService)
            fun defaultEntityConverter() = DefaultEntityConverter()
            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                defaultEntityConverter().parseCsvFile<ExerciseCsv>(it, exerciseCsvConverter())
            }.map { res -> res.value.first }.toList()

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

            val exerciseGroupRepository = Mockito.mock(ExerciseGroupRepository::class.java)
            fun groupConverter() = GroupUploader(exerciseGroupRepository)
            fun defaultEntityConverter() = DefaultEntityConverter()
            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                defaultEntityConverter().parseCsvFile<GroupCsv>(it, groupConverter())
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

            val seriesRepository = Mockito.mock(SeriesRepository::class.java)
            val exerciseGroupsService = Mockito.mock(ExerciseGroupsService::class.java)
            fun seriesCsvConverter() = SeriesUploader(seriesRepository, exerciseGroupsService)
            fun defaultEntityConverter() = DefaultEntityConverter()
            val result = input.byteInputStream(StandardCharsets.UTF_8).use {
                defaultEntityConverter().parseCsvFile<SeriesCsv>(it, seriesCsvConverter())
            }.map { res -> res.value.first }.toList()

            result shouldContain SeriesCsv(2, 1, "Распознование слов", "Распознование слов")
            result shouldContain SeriesCsv(2, 2, "Составление предложений", "Составление предложений")
        }
    }
})
