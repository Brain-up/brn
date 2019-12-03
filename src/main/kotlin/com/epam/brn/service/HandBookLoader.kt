package com.epam.brn.service

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.model.Resource
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.service.parsers.csv.CSVParserService
import com.epam.brn.service.parsers.csv.converter.Converter
import com.epam.brn.service.parsers.csv.dto.ExerciseCsv
import com.epam.brn.service.parsers.csv.dto.GroupCsv
import com.epam.brn.service.parsers.csv.dto.SeriesCsv
import com.epam.brn.service.parsers.csv.dto.TaskCsv
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * This class is responsible for
 * loading seed data on startup.
 */
@Service
@Profile("dev", "prod")
class HandBookLoader(
    private val exerciseGroupRepository: ExerciseGroupRepository,
    private val csvParserService: CSVParserService
) {

    @Value("\${init.folder:#{null}}")
    var folder: Path? = null

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        val isInitRequired = exerciseGroupRepository.count() == 0L

        if (isInitRequired) {
            folder?.let { loadInitialDataFromFileSystem(it) }
                ?: loadInitialDataFromClassPath()
        }
    }

    private fun loadInitialDataFromFileSystem(folder: Path) {
        if (!Files.exists(folder)) {
            throw IllegalArgumentException("$folder with intial data does not exist")
        }
        loadInitialDataToDb(folder)
    }

    private fun loadInitialDataFromClassPath() {
        val source = "initFiles"
        val sourcePath = Paths.get(
            javaClass.classLoader.getResource(source)?.toURI()
                ?: throw IllegalStateException("Classpath resource $source does not exist!")
        )

        loadInitialDataToDb(sourcePath)
    }

    private fun loadInitialDataToDb(source: Path) {
        val exercises = source.resolve(EXERCISES)
        require(Files.exists(exercises))

        val groups = source.resolve(GROUPS)
        require(Files.exists(groups))

        val series = source.resolve(SERIES)
        require(Files.exists(series))

        val tasks = source.resolve(TASKS)
        require(Files.exists(tasks))

        val groupsById = prepareExerciseGroups(groups)
        val seriesById = prepareSeries(groupsById, series)
        val exerciseById = prepareExercises(seriesById, exercises)
        prepareTasks(exerciseById, tasks)

        exerciseGroupRepository.saveAll(groupsById.values)
    }

    private fun prepareExerciseGroups(groups: Path): Map<Long, ExerciseGroup> {
        val groupsById = mutableMapOf<Long, ExerciseGroup>()
        val groupConverter = object : Converter<GroupCsv, ExerciseGroup> {
            override fun convert(source: GroupCsv): ExerciseGroup {
                val exerciseGroup = ExerciseGroup(name = source.name, description = source.description)
                groupsById[source.groupId] = exerciseGroup

                return exerciseGroup
            }
        }

        Files.newInputStream(groups).use {
            csvParserService.parseCommasSeparatedCsvFile(it, groupConverter)
        }
        return groupsById
    }

    private fun prepareSeries(
        groupsById: Map<Long, ExerciseGroup>,
        series: Path
    ): MutableMap<Long, Series> {
        val seriesById = mutableMapOf<Long, Series>()
        val seriesConverter = object : Converter<SeriesCsv, Series> {
            override fun convert(source: SeriesCsv): Series {
                require(groupsById.containsKey(source.groupId))

                val group = groupsById[source.groupId]!!
                val exerciseSeries = Series(
                    name = source.name,
                    description = source.description,
                    exerciseGroup = group
                )
                group.series += exerciseSeries

                seriesById[source.seriesId] = exerciseSeries
                return exerciseSeries
            }
        }

        Files.newInputStream(series).use {
            csvParserService.parseCommasSeparatedCsvFile(it, seriesConverter)
        }
        return seriesById
    }

    private fun prepareExercises(
        seriesById: MutableMap<Long, Series>,
        exercises: Path
    ): MutableMap<Long, Exercise> {
        val exerciseById = mutableMapOf<Long, Exercise>()
        val exerciseConverter = object : Converter<ExerciseCsv, Exercise> {
            override fun convert(source: ExerciseCsv): Exercise {
                require(seriesById.containsKey(source.seriesId))

                val exerciseSeries = seriesById[source.seriesId]!!

                val exercise = Exercise(
                    name = source.name,
                    description = source.description,
                    level = source.level,
                    series = exerciseSeries
                )

                exerciseById[source.exerciseId] = exercise

                exerciseSeries.exercises += exercise

                return exercise
            }
        }

        Files.newInputStream(exercises).use {
            csvParserService.parseCommasSeparatedCsvFile(it, exerciseConverter)
        }
        return exerciseById
    }

    private fun prepareTasks(
        exerciseById: MutableMap<Long, Exercise>,
        tasks: Path
    ) {
        val taskConverter = object : Converter<TaskCsv, Task> {
            override fun convert(source: TaskCsv): Task {
                require(exerciseById.containsKey(source.exerciseId))

                val answer = Resource(
                    word = source.word,
                    audioFileUrl = source.audioFileName,
                    pictureFileUrl = source.pictureFileName,
                    soundsCount = 1 // TODO need to detect sounds count
                )

                val options = source.words
                    .map { it.replace("[()]".toRegex(), "") }
                    .map { answer.copy(word = it) }
                    .toMutableSet()

                val exercise = exerciseById[source.exerciseId]!!
                val task = Task(
                    serialNumber = source.orderNumber,
                    exercise = exercise,
                    correctAnswer = answer,
                    answerOptions = options
                )

                exercise.tasks += task

                return task
            }
        }

        Files.newInputStream(tasks).use {
            csvParserService.parseCsvFile(it, taskConverter)
        }
    }

    companion object {
        private const val TASKS = "tasks.csv"
        private const val SERIES = "series.csv"
        private const val GROUPS = "groups.csv"
        private const val EXERCISES = "exercises.csv"
    }
}
