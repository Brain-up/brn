package com.epam.brn.csv.converter.impl.firstSeries

import com.epam.brn.constant.WordTypeEnum
import com.epam.brn.csv.converter.Uploader
import com.epam.brn.csv.dto.TaskCsv
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Resource
import com.epam.brn.model.Task
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.SeriesService
import com.epam.brn.service.TaskService
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SeriesOneUploader(
    private val exerciseService: ExerciseService,
    private val seriesService: SeriesService,
    private val taskService: TaskService,
    private val resourceService: ResourceService,
    @Value("\${brn.audio.file.default.path}") val defaultAudioFileUrl: String
) : Uploader<TaskCsv, Task> {

    private val log = logger()

    override fun convert(source: TaskCsv): Task {
        val target = Task()
        convertSerialNumber(source, target)
        convertExercise(source, target)
        convertCorrectAnswer(source, target)
        convertAnswers(source, target)
        setExerciseSeries(target)
        return target
    }

    override fun objectReader(): ObjectReader {
        val csvMapper = CsvMapper()

        val csvSchema = csvMapper
            .schemaFor(TaskCsv::class.java)
            .withColumnSeparator(' ')
            .withLineSeparator(" ")
            .withColumnReordering(true)
            .withArrayElementSeparator(",")
            .withHeader()

        return csvMapper
                .readerWithTypedSchemaFor(TaskCsv::class.java)
                .with(csvSchema)
    }

    override fun entityComparator(): (Task) -> Int {
        return { it -> it.exercise?.id?.toInt()!! }
    }

    override fun persistEntity(entity: Task) {
        taskService.save(entity)
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
