package com.epam.brn.service

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.parsers.csv.CSVParserService
import com.epam.brn.service.parsers.csv.converter.Converter
import com.epam.brn.service.parsers.csv.dto.ExerciseCsv
import com.epam.brn.service.parsers.csv.dto.GroupCsv
import com.epam.brn.service.parsers.csv.dto.SeriesCsv
import com.epam.brn.service.parsers.csv.dto.TaskCsv
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

/**
 * This class is responsible for
 * loading seed data on startup.
 */
@Service
@Profile("dev", "prod")
class InitialDataLoader(
    private val resourceLoader: ResourceLoader,
    private val exerciseGroupRepository: ExerciseGroupRepository,
    private val userAccountRepository: UserAccountRepository,
    private val csvParserService: CSVParserService
) {
    private val log = logger()

    @Value("\${init.folder:#{null}}")
    var folder: Path? = null

    @Value(value = "\${brn.audio.file.default.path}")
    private lateinit var defaultAudioFileUrl: String

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        userAccountRepository.save(UserAccount(name = "defaultUser", email = "default@default.ru"))
        userAccountRepository.save(UserAccount(name = "defaultUser2", email = "default2@default.ru"))

        val isInitRequired = exerciseGroupRepository.count() == 0L
        log.debug("Is initialization required: $isInitRequired")
        if (isInitRequired)
            folder?.let { loadInitialDataFromFileSystem(it) }
                ?: loadInitialDataFromClassPath()
    }

    private fun loadInitialDataFromFileSystem(folder: Path) {
        log.debug("Loading data from file system $folder")

        if (!Files.exists(folder))
            throw IllegalArgumentException("$folder with intial data does not exist")

        val sources = listOf(EXERCISES, GROUPS, SERIES, TASKS_FOR_SINGLE_WORDS_SERIES, TASKS_FOR_WORDS_SEQUENCES_SERIES)
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
        val sources = listOf(EXERCISES, GROUPS, SERIES, TASKS_FOR_SINGLE_WORDS_SERIES, TASKS_FOR_WORDS_SEQUENCES_SERIES)
            .map { Pair(it, resourceLoader.getResource("classpath:initFiles/$it").inputStream) }
            .toMap()
        loadInitialDataToDb(sources)
    }

    private fun loadInitialDataToDb(sources: Map<String, InputStream>) {
        val exercises = sources.getValue(EXERCISES)
        val groups = sources.getValue(GROUPS)
        val series = sources.getValue(SERIES)
        val tasksForSingleWordsSeries = sources.getValue(TASKS_FOR_SINGLE_WORDS_SERIES)
        val tasksForWordsSequencesSeries = sources.getValue(TASKS_FOR_WORDS_SEQUENCES_SERIES)

        val groupsById = prepareExerciseGroups(groups)
        val seriesById = prepareSeries(groupsById, series)
        val exerciseByName = prepareExercises(seriesById, exercises)
        prepareTasksForSingleWordsSeries(exerciseByName, tasksForSingleWordsSeries)
        prepareTasksForWordsSequencesSeries(seriesById, tasksForWordsSequencesSeries)
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
    ): MutableMap<Pair<String, Int>, Exercise> {
        val exerciseByName = mutableMapOf<Pair<String, Int>, Exercise>()
        val exerciseConverter = object : Converter<ExerciseCsv, Exercise> {
            override fun convert(source: ExerciseCsv): Exercise {
                val seriesId = source.seriesId
                require(seriesById.containsKey(seriesId))
                val exerciseSeries = seriesById[seriesId]!!
                val exerciseType =
                    if (seriesId == 1L) ExerciseTypeEnum.SINGLE_WORDS else ExerciseTypeEnum.WORDS_SEQUENCES
                val exercise = Exercise(
                    name = source.name,
                    description = source.description,
                    level = source.level,
                    series = exerciseSeries,
                    exerciseType = exerciseType.toString()
                )
                exerciseByName[Pair(source.name, source.level)] = exercise
                exerciseSeries.exercises += exercise
                return exercise
            }
        }
        csvParserService.parseCommasSeparatedCsvFile(exercisesInputStream, exerciseConverter)
        return exerciseByName
    }

    private fun prepareTasksForSingleWordsSeries(
        exerciseByNameAndLevel: MutableMap<Pair<String, Int>, Exercise>,
        tasksInputStream: InputStream
    ) {
        val taskConverter = object : Converter<TaskCsv, Task> {
            override fun convert(source: TaskCsv): Task {
                val exerciseName = source.exerciseName
                val level = source.level
                require(exerciseByNameAndLevel.containsKey(Pair(exerciseName, level)))

                val answer = Resource(
                    word = source.word,
                    audioFileUrl = source.audioFileName,
                    pictureFileUrl = source.pictureFileName,
                    soundsCount = 1, // TODO need to detect sounds count
                    wordType = WordTypeEnum.valueOf(source.wordType).toString()
                )

                val options = source.words
                    .map { it.replace("[()]".toRegex(), "") }
                    .map { answer.copy(word = it) }
                    .toMutableSet()

                val exercise = exerciseByNameAndLevel[Pair(exerciseName, level)]!!
                val task = Task(
                    serialNumber = source.orderNumber,
                    exercise = exercise,
                    correctAnswer = answer,
                    answerOptions = options
                )
                exercise.exerciseType = ExerciseTypeEnum.SINGLE_WORDS.toString()
                exercise.tasks += task
                return task
            }
        }
        csvParserService.parseCsvFile(tasksInputStream, taskConverter)
    }

    // todo: get data from file
    private fun prepareTasksForWordsSequencesSeries(
        seriesById: MutableMap<Long, Series>,
        tasksInputStream: InputStream
    ) {
        val resource1 = Resource(
            word = "девочка",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/девочка.mp3",
            pictureFileUrl = "pictures/withWord/девочка.jpg"
        )
        val resource2 = Resource(
            word = "дедушка",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/дедушка.mp3",
            pictureFileUrl = "pictures/withWord/дедушка.jpg"
        )
        val resource3 = Resource(
            word = "бабушка",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/бабушка.mp3",
            pictureFileUrl = "pictures/withWord/бабушка.jpg"
        )
        val resource4 = Resource(
            word = "бросает",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/бросает.mp3",
            pictureFileUrl = "pictures/withWord/бросает.jpg"
        )
        val resource5 = Resource(
            word = "читает",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/читает.mp3",
            pictureFileUrl = "pictures/withWord/читает.jpg"
        )
        val resource6 = Resource(
            word = "рисует",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/рисует.mp3",
            pictureFileUrl = "pictures/withWord/рисует.jpg"
        )

        val task = Task(
            serialNumber = 1,
            answerOptions = mutableSetOf(resource1, resource2, resource3, resource4, resource5, resource6)
        )
        val exercise = Exercise(
            series = seriesById[2L]!!,
            name = "Распознование последовательности слов",
            description = "Распознование последовательности слов",
            template = "<OBJECT OBJECT_ACTION>",
            exerciseType = ExerciseTypeEnum.WORDS_SEQUENCES.toString(),
            level = 1
        )
        task.exercise = exercise
        exercise.tasks.add(task)
        (seriesById[1L] as Series).exercises.add(exercise)
    }

    companion object {
        private const val TASKS_FOR_SINGLE_WORDS_SERIES = "tasks_for_single_words_series.csv"
        private const val TASKS_FOR_WORDS_SEQUENCES_SERIES = "tasks_for_words_sequences_series.csv"
        private const val SERIES = "series.csv"
        private const val GROUPS = "groups.csv"
        private const val EXERCISES = "exercises.csv"
    }
}
