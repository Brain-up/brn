package com.epam.brn.upload.csv.series1

import com.epam.brn.enums.AudiometryType
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.Resource
import com.epam.brn.model.WordType
import com.epam.brn.repo.AudiometryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.service.WordsService
import com.epam.brn.upload.csv.RecordProcessor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LopotkoRecordProcessor(
    private val audiometryRepository: AudiometryRepository,
    private val audiometryTaskRepository: AudiometryTaskRepository,
    private val resourceRepository: ResourceRepository,
    private val wordsService: WordsService
) : RecordProcessor<LopotkoRecord, AudiometryTask> {

    @Value(value = "\${lopotkoFileName}")
    private lateinit var lopotkoFileName: String

    @Value(value = "\${audioPath}")
    private lateinit var audioPathFilipp: String

    @Value(value = "\${audioPathAlena}")
    private lateinit var audioPathAlena: String

    val mapHashWord = mutableMapOf<String, String>()

    override fun isApplicable(record: Any): Boolean = record is LopotkoRecord

    @Transactional
    override fun process(records: List<LopotkoRecord>): List<AudiometryTask> {
        val audiometryTasks = mutableSetOf<AudiometryTask>()

        records.forEach {
            val answerOptions: MutableSet<Resource> = extractAnswerOptions(it)
            resourceRepository.saveAll(answerOptions)

            val audiometryTask = extractAudiometryTask(it, answerOptions)
            audiometryTasks.add(audiometryTaskRepository.save(audiometryTask))
        }
        wordsService.createTxtFileWithExerciseWordsMap(mapHashWord, lopotkoFileName)
        return audiometryTasks.toMutableList()
    }

    private fun extractAnswerOptions(record: LopotkoRecord): MutableSet<Resource> {
        var audioPath = audioPathFilipp
//      todo: think about voice gender! if (record.exerciseName.startsWith("М")) audioPath = audioPathAlena
        return record.words
            .asSequence()
            .map { it.replace("[()]".toRegex(), StringUtils.EMPTY) }
            .map { toResource(it, audioPath) }
            .toMutableSet()
    }

    private fun toResource(word: String, audioPath: String): Resource {
        val hashWord = DigestUtils.md5Hex(word)
        mapHashWord[word] = hashWord
        val audioFileUrl = audioPath.format(hashWord)
        val resource = resourceRepository.findFirstByWordAndAudioFileUrlLike(word, audioFileUrl)
            .orElse(
                Resource(
                    word = word,
                    audioFileUrl = audioFileUrl
                )
            )
        resource.wordType = WordType.AUDIOMETRY_WORD.toString()
        return resource
    }

    private fun extractAudiometryTask(record: LopotkoRecord, answerOptions: MutableSet<Resource>): AudiometryTask {
        val audiometry = audiometryRepository.findByAudiometryTypeAndLocale(
            AudiometryType.valueOf(record.type).name,
            record.locale.locale
        )!!
        return AudiometryTask(
            level = record.order,
            audiometryGroup = record.group,
            frequencyZone = record.frequencyZone.name,
            minFrequency = record.minFrequency,
            maxFrequency = record.maxFrequency,
            audiometry = audiometry,
            answerOptions = answerOptions
        )
    }
}
