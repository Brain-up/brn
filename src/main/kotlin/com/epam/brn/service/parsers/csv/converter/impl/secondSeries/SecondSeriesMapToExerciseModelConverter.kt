package com.epam.brn.service.parsers.csv.converter.impl.secondSeries

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.constant.mapPositionToWordType
import com.epam.brn.model.Exercise
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.service.ResourceService
import com.epam.brn.service.SeriesService
import com.epam.brn.service.parsers.csv.converter.Converter
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SecondSeriesMapToExerciseModelConverter : Converter<Map<String, Any>, Exercise> {

    val EXERCISE_NAME = "exerciseName"
    val LEVEL = "level"
    val WORDS = "words"

    @Value(value = "\${brn.audio.file.default.path}")
    private lateinit var defaultAudioFileUrl: String

    @Autowired
    lateinit var resourceService: ResourceService

    @Autowired
    lateinit var seriesService: SeriesService

    override fun convert(source: Map<String, Any>): Exercise {
        val target = Exercise()
        convertExercise(source, target)
        convertTask(target)
        convertResources(source, target)

        return target
    }

    private fun convertExercise(source: Map<String, Any>, target: Exercise) {
        target.name = source[EXERCISE_NAME].toString()
        target.description = source[EXERCISE_NAME].toString()
        target.level = source[LEVEL].toString().toInt()
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
        else
            resources.first()
    }

    private fun createAndGetResource(word: String, wordType: String): Resource {
        val resource = Resource()
        resource.word = word
        resource.wordType = WordTypeEnum.valueOf(wordType).toString()
        resource.audioFileUrl = defaultAudioFileUrl.format(word)

        return resource
    }

    private fun setWordType(resource: Resource, wordType: WordTypeEnum?): Resource {
        resource.wordType = wordType?.toString() ?: WordTypeEnum.UNKNOWN.toString()

        return resource
    }
}
