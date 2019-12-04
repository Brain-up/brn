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
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * This class is responsible for
 * loading seed data on startup.
 */
@Service
@Profile("dev", "prod")
class HandBookLoader(
    private val resourceLoader: ResourceLoader,
    private val exerciseGroupRepository: ExerciseGroupRepository,
    private val csvParserService: CSVParserService
) {

    private val log = logger()

    @Value("\${init.folder:#{null}}")
    var folder: Path? = null

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        val isInitRequired = exerciseGroupRepository.count() == 0L

        log.debug("Is initialization required: $isInitRequired")

        if (isInitRequired) {
            folder?.let { loadInitialDataFromFileSystem(it) }
                ?: loadInitialDataFromClassPath()
        }
    }

    private fun loadInitialDataFromFileSystem(folder: Path) {
        log.debug("Loading data from file system $folder")

        if (!Files.exists(folder)) {
            throw IllegalArgumentException("$folder with intial data does not exist")
        }
        val sources = listOf(EXERCISES, GROUPS, SERIES, TASKS)
            .map { Pair(it, Files.newInputStream(folder.resolve(it))) }
            .toMap()

        try {
            loadInitialDataToDb(sources)
        } finally {
            sources.onEach { (_, inputStream) ->
                try {
                    inputStream.close()
                } catch (e: Exception) {
                    log.error(e)
                }
            }
        }
    }

    private fun loadInitialDataFromClassPath() {
        log.debug("Loading data from classpath initFiles")

        val sources = listOf(EXERCISES, GROUPS, SERIES, TASKS)
            .map { Pair(it, resourceLoader.getResource("classpath:initFiles/$it").inputStream) }
            .toMap()

        loadInitialDataToDb(sources)
    }

    private fun loadInitialDataToDb(sources: Map<String, InputStream>) {
        val exercises = sources.getValue(EXERCISES)
        val groups = sources.getValue(GROUPS)
        val series = sources.getValue(SERIES)
        val tasks = sources.getValue(TASKS)

        val groupsById = prepareExerciseGroups(groups)
        val seriesById = prepareSeries(groupsById, series)
        val exerciseById = prepareExercises(seriesById, exercises)
        prepareTasks(exerciseById, tasks)

        exerciseGroupRepository.saveAll(groupsById.values)

        log.debug("Initialization succeeded")
    }

    private fun prepareExerciseGroups(groupsInputStream: InputStream): Map<Long, ExerciseGroup> {
        val groupsById = mutableMapOf<Long, ExerciseGroup>()
        val groupConverter = object : Converter<GroupCsv, ExerciseGroup> {
            override fun convert(source: GroupCsv): ExerciseGroup {
                val exerciseGroup = ExerciseGroup(name = source.name, description = source.description)
                groupsById[source.groupId] = exerciseGroup

                return exerciseGroup
            }
        }

        csvParserService.parseCommasSeparatedCsvFile(groupsInputStream, groupConverter)

        return groupsById
    }

    private fun prepareSeries(
        groupsById: Map<Long, ExerciseGroup>,
        seriesInputStream: InputStream
    ): MutableMap<Long, Series> {
        val seriesById = mutableMapOf<Long, Series>()
        val seriesConverter = object : Converter<SeriesCsv, Series> {
            override fun convert(source: SeriesCsv): Series {
                require(groupsById.containsKey(source.groupId))

                val group = groupsById.getValue(source.groupId)
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

        csvParserService.parseCommasSeparatedCsvFile(seriesInputStream, seriesConverter)

        return seriesById
    }

    private fun prepareExercises(
        seriesById: MutableMap<Long, Series>,
        exercisesInputStream: InputStream
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

        csvParserService.parseCommasSeparatedCsvFile(exercisesInputStream, exerciseConverter)
        return exerciseById
    }

    private fun prepareTasks(
        exerciseById: MutableMap<Long, Exercise>,
        tasksInputStream: InputStream
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

        csvParserService.parseCsvFile(tasksInputStream, taskConverter)
    }

    companion object {
        private const val TASKS = "tasks.csv"
        private const val SERIES = "series.csv"
        private const val GROUPS = "groups.csv"
        private const val EXERCISES = "exercises.csv"
    }
}
