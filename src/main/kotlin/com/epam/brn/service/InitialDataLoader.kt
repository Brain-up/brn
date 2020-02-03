package com.epam.brn.service

import com.epam.brn.constant.BrnRoles.AUTH_ROLE_ADMIN
import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.model.Authority
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.parsers.csv.FirstSeriesCSVParserService
import com.epam.brn.service.parsers.csv.converter.impl.ExerciseCsvToExerciseModelConverter
import com.epam.brn.service.parsers.csv.converter.impl.ExerciseGroupCsvToExerciseGroupConverter
import com.epam.brn.service.parsers.csv.converter.impl.SeriesCsvToSeriesConverter
import com.epam.brn.service.parsers.csv.converter.impl.TaskCsvToTaskModelConverter
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.core.io.ResourceLoader
import org.springframework.security.crypto.password.PasswordEncoder
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
    private val firstSeriesCsvParserService: FirstSeriesCSVParserService,
    private val passwordEncoder: PasswordEncoder
) {
    private val log = logger()

    @Value("\${init.folder:#{null}}")
    var folder: Path? = null

    @Autowired
    lateinit var exerciseModelConverter: ExerciseCsvToExerciseModelConverter

    @Autowired
    lateinit var exerciseGroupCsvToExerciseGroupConverter: ExerciseGroupCsvToExerciseGroupConverter

    @Autowired
    lateinit var seriesCsvToSeriesConverter: SeriesCsvToSeriesConverter

    @Autowired
    lateinit var taskCsvToTaskModelConverter: TaskCsvToTaskModelConverter

    @Autowired
    lateinit var exerciseService: ExerciseService

    @Autowired
    lateinit var seriesService: SeriesService

    @Autowired
    lateinit var resourceService: ResourceService

    @Autowired
    lateinit var exerciseGroupService: ExerciseGroupsService

    @Autowired
    lateinit var taskService: TaskService

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        addAdminUser()
        userAccountRepository.save(
            UserAccount(
                userName = "defaultUser",
                email = "default@default.ru",
                active = true,
                password = "password"
            )
        )
        userAccountRepository.save(
            UserAccount(
                userName = "defaultUser2",
                email = "default2@default.ru",
                active = true,
                password = "password"
            )
        )

        val isInitRequired = exerciseGroupRepository.count() == 0L
        log.debug("Is initialization required: $isInitRequired")
        if (isInitRequired)
            folder?.let { loadInitialDataFromFileSystem(it) }
                ?: loadInitialDataFromClassPath()
    }

    private fun addAdminUser() {
        val password = passwordEncoder.encode("admin")
        val userAccount =
            UserAccount(userName = "admin", password = password, email = "admin@admin.com", active = true)
        userAccount.authoritySet.addAll(setOf(Authority(authority = AUTH_ROLE_ADMIN, userAccount = userAccount)))
        userAccountRepository.save(userAccount)
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
        loadExerciseGroups(sources.getValue(GROUPS))
        loadSeries(sources.getValue(SERIES))
        loadExercises(sources.getValue(EXERCISES))
        loadTasksForSingleWordsSeries(sources.getValue(TASKS_FOR_SINGLE_WORDS_SERIES))

        prepareTasksForWordsSequencesSeries(sources.getValue(TASKS_FOR_WORDS_SEQUENCES_SERIES))
        log.debug("Initialization succeeded")
    }

    private fun loadExerciseGroups(groupsInputStream: InputStream) {
        firstSeriesCsvParserService.parseCommasSeparatedCsvFile(groupsInputStream, exerciseGroupCsvToExerciseGroupConverter)
            .map(exerciseGroupService::save)
    }

    private fun loadSeries(seriesInputStream: InputStream) {
        firstSeriesCsvParserService.parseCommasSeparatedCsvFile(seriesInputStream, seriesCsvToSeriesConverter)
            .map(seriesService::save)
    }

    private fun loadExercises(exercisesInputStream: InputStream) {
        firstSeriesCsvParserService.parseCommasSeparatedCsvFile(exercisesInputStream, exerciseModelConverter)
            .map(exerciseService::save)
    }

    private fun loadTasksForSingleWordsSeries(tasksInputStream: InputStream) {
        firstSeriesCsvParserService.parseCsvFile(tasksInputStream, taskCsvToTaskModelConverter)
            .map(Map.Entry<String, Pair<Task?, String?>>::value)
            .map(Pair<Task?, String?>::first)
            .map { value -> value!! }
            .map(taskService::save)
    }

    // todo: get data from file
    private fun prepareTasksForWordsSequencesSeries(tasksInputStream: InputStream) {
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

        resourceService.saveAll(listOf(resource1, resource2, resource3, resource4, resource5, resource6))

        val task2 = Task(
            serialNumber = 1,
            answerOptions = mutableSetOf(resource1, resource2, resource3, resource4, resource5, resource6)
        )
        val exercise2 = Exercise(
            series = seriesService.findSeriesForId(2L),
            name = "Распознование последовательности слов",
            description = "Распознование последовательности слов",
            template = "<OBJECT OBJECT_ACTION>",
            exerciseType = ExerciseTypeEnum.WORDS_SEQUENCES.toString(),
            level = 1
        )

        task2.exercise = exercise2
        exercise2.tasks.add(task2)
        seriesService.findSeriesWithExercisesForId(2L).exercises.add(exercise2)

        exerciseService.save(exercise2)

        // for 3 series
        val resource7 = Resource(
            word = "девочка рисует",
            wordType = WordTypeEnum.SENTENCE.toString(),
            audioFileUrl = "series3/девочка_рисует.mp3"
        )
        val task3 = Task(
            serialNumber = 2,
            answerOptions = mutableSetOf(resource1, resource2, resource3, resource4, resource5, resource6),
            correctAnswer = resource7,
            answerParts = mutableMapOf(1 to resource1, 2 to resource6)
        )
        val exercise3 = Exercise(
            series = seriesService.findSeriesForId(3L),
            name = "Распознование предложений из 2 слов",
            description = "Распознование предложений из 2 слов",
            template = "<OBJECT OBJECT_ACTION>",
            exerciseType = ExerciseTypeEnum.SENTENCE.toString(),
            level = 1
        )

        task3.exercise = exercise3
        exercise3.tasks.add(task3)
        seriesService.findSeriesWithExercisesForId(3L).exercises.add(exercise3)

        exerciseService.save(exercise3)
    }

    companion object {
        private const val TASKS_FOR_SINGLE_WORDS_SERIES = "tasks_for_single_words_series.csv"
        private const val TASKS_FOR_WORDS_SEQUENCES_SERIES = "tasks_for_words_sequences_series.csv"
        private const val SERIES = "series.csv"
        private const val GROUPS = "groups.csv"
        private const val EXERCISES = "exercises.csv"
    }
}
