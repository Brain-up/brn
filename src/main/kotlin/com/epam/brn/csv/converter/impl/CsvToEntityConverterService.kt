package com.epam.brn.csv.converter.impl

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.constant.mapPositionToWordType
import com.epam.brn.csv.converter.CsvToEntityConverter
import com.epam.brn.csv.dto.ExerciseCsv
import com.epam.brn.csv.dto.GroupCsv
import com.epam.brn.csv.dto.SeriesCsv
import com.epam.brn.csv.dto.TaskCsv
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.service.ExerciseGroupsService
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.SeriesService
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service

@Service
class CsvToEntityConverterService {

    private val log = logger()

    @Bean
    fun exerciseConverter(seriesService: SeriesService) = object : CsvToEntityConverter<ExerciseCsv, Exercise> {
        override fun convert(source: ExerciseCsv): Exercise {
            val target = Exercise()
            convertSeries(source, target)
            convertExerciseType(source, target)
            target.name = source.name
            target.level = source.level
            target.description = source.description
            target.id = source.exerciseId
            return target
        }

        private fun convertSeries(source: ExerciseCsv, target: Exercise) {
            target.series = seriesService.findSeriesForId(source.seriesId)
        }

        private fun convertExerciseType(source: ExerciseCsv, target: Exercise) {
            val exerciseType =
                when (source.seriesId) {
                    1L -> ExerciseTypeEnum.SINGLE_WORDS
                    2L -> ExerciseTypeEnum.WORDS_SEQUENCES
                    3L -> ExerciseTypeEnum.SENTENCE
                    else -> throw IllegalArgumentException("There no ExerciseType for seriesId=${source.seriesId}")
                }
            target.exerciseType = exerciseType.toString()
        }
    }

    @Bean
    fun groupConverter() = object : CsvToEntityConverter<GroupCsv, ExerciseGroup> {
        override fun convert(source: GroupCsv): ExerciseGroup {
            return ExerciseGroup(name = source.name, description = source.description, id = source.groupId)
        }
    }

    @Bean
    fun seriesOneConverter(
        resourceService: ResourceService,
        exerciseService: ExerciseService,
        seriesService: SeriesService,
        @Value("\${brn.audio.file.default.path}") defaultAudioFileUrl: String
    ) = object : CsvToEntityConverter<TaskCsv, Task> {
        override fun convert(source: TaskCsv): Task {
            val target = Task()
            convertSerialNumber(source, target)
            convertExercise(source, target)
            convertCorrectAnswer(source, target)
            convertAnswers(source, target)
            setExerciseSeries(target)
            return target
        }

        private fun setExerciseSeries(taskFile: Task?) {
            taskFile?.exercise?.series = seriesService.findSeriesForId(1)
        }

        private fun convertSerialNumber(source: TaskCsv, target: Task) {
            target.serialNumber = source.orderNumber
        }

        private fun convertExercise(source: TaskCsv, target: Task) {
            try {
                target.exercise = exerciseService.findExerciseByNameAndLevel(source.exerciseName, source.level)
            } catch (e: EntityNotFoundException) {
                log.debug("Entity was not found by name ${source.exerciseName}")
                target.exercise = exerciseService.createExercise(source.exerciseName)
            }
        }

        private fun convertCorrectAnswer(source: TaskCsv, target: Task) {
            val word = source.word
            val wordType = source.wordType
            val audioFileName = source.audioFileName
            val resources = resourceService.findByWordAndAudioFileUrlLike(word, audioFileName)
            val correctAnswer: Resource
            if (CollectionUtils.isEmpty(resources)) {
                correctAnswer = createAndGetResource(word, audioFileName, source.pictureFileName, wordType)
            } else {
                correctAnswer = resources[0]
                correctAnswer.pictureFileUrl = source.pictureFileName
                correctAnswer.wordType = wordType
            }
            target.correctAnswer = resourceService.save(correctAnswer)
        }

        private fun convertAnswers(source: TaskCsv, target: Task) {
            target.answerOptions = CollectionUtils.emptyIfNull(source.words)
                .asSequence()
                .filter { StringUtils.isNotEmpty(it) }
                .map { word -> word.replace("[()]".toRegex(), "") }
                .map(this::getResourceByWord)
                .map(resourceService::save)
                .toMutableSet()
        }

        private fun getResourceByWord(word: String): Resource {
            val resources = resourceService.findByWordLike(word)
            return if (CollectionUtils.isEmpty(resources))
                createAndGetResource(word, StringUtils.EMPTY, StringUtils.EMPTY, WordTypeEnum.UNKNOWN.toString())
            else
                resources.first()
        }

        private fun createAndGetResource(
            word: String,
            audioFileName: String,
            pictureFileName: String,
            wordType: String
        ): Resource {
            val resource = Resource()
            resource.word = word
            resource.wordType = WordTypeEnum.valueOf(wordType).toString()
            resource.audioFileUrl =
                if (StringUtils.isNotEmpty(audioFileName)) audioFileName else defaultAudioFileUrl.format(word)
            resource.pictureFileUrl =
                if (StringUtils.isNotEmpty(pictureFileName)) pictureFileName else null
            return resource
        }
    }

    @Bean
    fun seriesConverter(exerciseGroupsService: ExerciseGroupsService) =
        object : CsvToEntityConverter<SeriesCsv, Series> {
            override fun convert(source: SeriesCsv): Series {
                return Series(
                    name = source.name,
                    description = source.description,
                    exerciseGroup = exerciseGroupsService.findGroupById(source.groupId),
                    id = source.seriesId
                )
            }
        }

    @Bean
    fun seriesTwoConverter(
        resourceService: ResourceService,
        seriesService: SeriesService,
        exerciseService: ExerciseService,
        @Value("\${brn.audio.file.second.series.path}") audioFileUrl: String,
        @Value("\${brn.picture.file.default.path}") pictureFileUrl: String
    ) = object : CsvToEntityConverter<Map<String, Any>, Exercise> {

        val EXERCISE_NAME = "exerciseName"
        val LEVEL = "level"
        val WORDS = "words"

        override fun convert(source: Map<String, Any>): Exercise {
            val target = createOrGetExercise(source)
            convertExercise(source, target)
            convertTask(target)
            convertResources(source, target)

            return target
        }

        private fun createOrGetExercise(source: Map<String, Any>): Exercise {
            val name = source[EXERCISE_NAME].toString()
            val level = source[LEVEL].toString().toInt()

            return try {
                val exercise = exerciseService.findExerciseByNameAndLevel(name, level)

                log.debug("exercise with name {$name} and level {$level} is already persisted. Entity Will be updated")

                exercise
            } catch (e: EntityNotFoundException) {
                Exercise(name = name, level = level)
            }
        }

        private fun convertExercise(source: Map<String, Any>, target: Exercise) {
            target.description = source[EXERCISE_NAME].toString()
            target.exerciseType = ExerciseTypeEnum.WORDS_SEQUENCES.toString()
            target.series = seriesService.findSeriesForId(2L)
        }

        private fun convertTask(target: Exercise) {
            val task = Task()
            task.serialNumber = 2
            task.exercise = target
            target.tasks.add(task)
        }

        private fun convertResources(source: Map<String, Any>, target: Exercise) {
            val templateList = arrayListOf<String>()

            source[WORDS]
                .toString()
                .split(";")
                .mapIndexed { index, element ->
                    val wordType = mapPositionToWordType[index]
                    val resources = createOrGetResources(element, wordType)

                    if (resources.isNotEmpty()) {
                        templateList.add(wordType.toString())
                    }

                    resources
                }
                .map { resource -> target.tasks.first().answerOptions.addAll(resource) }

            target.template = templateList.joinToString(StringUtils.SPACE, "<", ">")
        }

        private fun createOrGetResources(words: String, wordType: WordTypeEnum?): List<Resource> {
            return words.split(StringUtils.SPACE)
                .asSequence()
                .map { word -> word.replace("[()]".toRegex(), StringUtils.EMPTY) }
                .filter { word -> StringUtils.isNotEmpty(word) }
                .map { word -> getResourceByWord(word) }
                .map { resource -> setWordType(resource, wordType) }
                .map(resourceService::save)
                .toList()
        }

        private fun getResourceByWord(word: String): Resource {
            val resources = resourceService.findByWordLike(word)
            return if (CollectionUtils.isEmpty(resources))
                createAndGetResource(word, WordTypeEnum.UNKNOWN.toString())
            else {
                log.debug("Resource with word {$word} was already persisted")
                resources.first()
            }
        }

        private fun createAndGetResource(word: String, wordType: String): Resource {
            val resource = Resource()
            resource.word = word
            resource.wordType = WordTypeEnum.valueOf(wordType).toString()
            resource.audioFileUrl = audioFileUrl.format(word)
            resource.pictureFileUrl = pictureFileUrl.format(word)

            return resource
        }

        private fun setWordType(resource: Resource, wordType: WordTypeEnum?): Resource {
            resource.wordType = wordType?.toString() ?: WordTypeEnum.UNKNOWN.toString()
            log.debug("Word type for resource with id {${resource.id}} was updated to {${resource.wordType}}")
            return resource
        }
    }
}
