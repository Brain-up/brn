package com.epam.brn.service

import com.epam.brn.constant.BrnRoles.AUTH_ROLE_ADMIN
import com.epam.brn.constant.BrnRoles.AUTH_ROLE_USER
import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.csv.CsvMappingIteratorParser
import com.epam.brn.csv.converter.Converter
import com.epam.brn.csv.converter.impl.firstSeries.ExerciseCsvConverter
import com.epam.brn.csv.converter.impl.firstSeries.GroupCsvConverter
import com.epam.brn.csv.converter.impl.firstSeries.SeriesCsvConverter
import com.epam.brn.csv.converter.impl.firstSeries.TaskCsv1SeriesConverter
import com.epam.brn.csv.converter.impl.secondSeries.Exercise2SeriesConverter
import com.epam.brn.model.Authority
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.repo.UserAccountRepository
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

// TODO: this class smells like GOD-object anti-pattern, it should be split

/**
 * This class is responsible for
 * loading seed data on startup.
 */
@Service
@Profile("dev", "prod")
class InitialDataLoader(
    private val resourceLoader: ResourceLoader,
    private val exerciseGroupRepository: ExerciseGroupRepository,
    private val seriesRepository: SeriesRepository,
    private val exerciseRepository: ExerciseRepository,
    private val taskRepository: TaskRepository,
    private val userAccountRepository: UserAccountRepository,
    private val csvMappingIteratorParser: CsvMappingIteratorParser,
    private val passwordEncoder: PasswordEncoder,
    private val authorityService: AuthorityService
) {
    private val log = logger()

    @Value("\${init.folder:#{null}}")
    var folder: Path? = null

    @Autowired
    lateinit var groupCsvConverter: GroupCsvConverter

    @Autowired
    lateinit var exerciseCsvConverter: ExerciseCsvConverter

    @Autowired
    lateinit var seriesCsvConverter: SeriesCsvConverter

    @Autowired
    lateinit var taskCsv1SeriesConverter: TaskCsv1SeriesConverter

    @Autowired
    lateinit var exercise2SeriesConverter: Exercise2SeriesConverter

    @Autowired
    lateinit var exerciseService: ExerciseService

    @Autowired
    lateinit var seriesService: SeriesService

    @Autowired
    lateinit var resourceService: ResourceService

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        val adminAuthority = authorityService.save(Authority(authorityName = AUTH_ROLE_ADMIN))
        val userAuthority = authorityService.save(Authority(authorityName = AUTH_ROLE_USER))
        val admin = addAdminUser(adminAuthority)
        val listOfUsers = addDefaultUsers(userAuthority)
        listOfUsers.add(admin)

        userAccountRepository.saveAll(listOfUsers)

        val isInitRequired = exerciseGroupRepository.count() == 0L
        log.debug("Is initialization required: $isInitRequired")
        if (isInitRequired)
            folder?.let { loadInitialDataFromFileSystem(it) }
                ?: loadInitialDataFromClassPath()
    }

    private fun addDefaultUsers(userAuthority: Authority): MutableList<UserAccount> {
        val password = passwordEncoder.encode("password")
        val firstUser = UserAccount(
            firstName = "firstName",
            lastName = "lastName",
            email = "default@default.ru",
            active = true,
            password = password
        )
        val secondUser = UserAccount(
            firstName = "firstName2",
            lastName = "lastName2",
            email = "default2@default.ru",
            active = true,
            password = password
        )
        firstUser.authoritySet.addAll(setOf(userAuthority))
        secondUser.authoritySet.addAll(setOf(userAuthority))
        return mutableListOf(firstUser, secondUser)
    }

    private fun addAdminUser(adminAuthority: Authority): UserAccount {
        val password = passwordEncoder.encode("admin")
        val userAccount =
            UserAccount(firstName = "admin", lastName = "admin", password = password, email = "admin@admin.com", active = true)
        userAccount.authoritySet.addAll(setOf(adminAuthority))
        return userAccount
    }

    private fun loadInitialDataFromFileSystem(folder: Path) {
        log.debug("Loading data from file system $folder")

        if (!Files.exists(folder))
            throw IllegalArgumentException("$folder with intial data does not exist")

        val sources = listOf(GROUPS, SERIES, EXERCISES, fileNameForSeries(1), fileNameForSeries(2), fileNameForSeries(3))
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
        val sources = listOf(GROUPS, SERIES, EXERCISES, fileNameForSeries(1), fileNameForSeries(2), fileNameForSeries(3))
            .map { Pair(it, resourceLoader.getResource("classpath:initFiles/$it").inputStream) }
            .toMap()
        loadInitialDataToDb(sources)
    }

    fun <CsvType, EntityType> parseEntities(inputStream: InputStream, converter: Converter<CsvType, EntityType>): List<EntityType?> {
        return this@InitialDataLoader.csvMappingIteratorParser.parseCsvFile(
            inputStream,
            converter
        )
            .map(Map.Entry<String, Pair<EntityType?, String?>>::value)
            .map(Pair<EntityType?, String?>::first)
            .toList()
    }

    private fun loadInitialDataToDb(sources: Map<String, InputStream>) {
        val parsedEntities = parseEntities(sources.getValue(GROUPS), groupCsvConverter)
        exerciseGroupRepository.saveAll(parsedEntities.sortedBy { it?.id })

        val seriesEntities = parseEntities(sources.getValue(SERIES), seriesCsvConverter)
        seriesRepository.saveAll(seriesEntities.sortedBy { it?.id })

        var exerciseEntities = parseEntities(sources.getValue(EXERCISES), exerciseCsvConverter)
        exerciseRepository.saveAll(exerciseEntities.sortedBy { it?.id })

        var series1 = parseEntities(sources.getValue(fileNameForSeries(1)), taskCsv1SeriesConverter)
        taskRepository.saveAll(series1.sortedBy { it!!.exercise!!.id })

        var series2 = parseEntities(sources.getValue(fileNameForSeries(2)), exercise2SeriesConverter)
        exerciseRepository.saveAll(series2.sortedBy { it!!.level })

        loadTasksFor3Series(sources.getValue(fileNameForSeries(3)))
        log.debug("Initialization succeeded")
    }

    // todo: get data from file for 3 series
    private fun loadTasksFor3Series(tasksInputStream: InputStream) {
        val resource1 = Resource(
            word = "девочкаTest",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/девочка.mp3",
            pictureFileUrl = "pictures/withWord/девочка.jpg"
        )
        val resource2 = Resource(
            word = "дедушкаTest",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/дедушка.mp3",
            pictureFileUrl = "pictures/withWord/дедушка.jpg"
        )
        val resource3 = Resource(
            word = "бабушкаTest",
            wordType = WordTypeEnum.OBJECT.toString(),
            audioFileUrl = "series2/бабушка.mp3",
            pictureFileUrl = "pictures/withWord/бабушка.jpg"
        )
        val resource4 = Resource(
            word = "бросаетTest",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/бросает.mp3",
            pictureFileUrl = "pictures/withWord/бросает.jpg"
        )
        val resource5 = Resource(
            word = "читаетTest",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/читает.mp3",
            pictureFileUrl = "pictures/withWord/читает.jpg"
        )
        val resource6 = Resource(
            word = "рисуетTest",
            wordType = WordTypeEnum.OBJECT_ACTION.toString(),
            audioFileUrl = "series2/рисует.mp3",
            pictureFileUrl = "pictures/withWord/рисует.jpg"
        )
        resourceService.saveAll(listOf(resource1, resource2, resource3, resource4, resource5, resource6))

        // for 3 series
        val resource7 = Resource(
            word = "девочка рисует",
            wordType = WordTypeEnum.SENTENCE.toString(),
            audioFileUrl = "series3/девочка_рисует.mp3"
        )
        resourceService.save(resource7)

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
        fun fileNameForSeries(seriesId: Long) = "${seriesId}_series.csv"
        private const val SERIES = "series.csv"
        private const val GROUPS = "groups.csv"
        private const val EXERCISES = "exercises.csv"
    }
}
