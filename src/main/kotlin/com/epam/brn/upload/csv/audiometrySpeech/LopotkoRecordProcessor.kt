package com.epam.brn.upload.csv.series1

import com.epam.brn.enums.AudiometryType
import com.epam.brn.enums.Locale
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.Resource
import com.epam.brn.model.WordType
import com.epam.brn.repo.AudiometryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.service.AudioFileMetaData
import com.epam.brn.service.WordsService
import com.epam.brn.upload.csv.RecordProcessor
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LopotkoRecordProcessor(
    private val audiometryRepository: AudiometryRepository,
    private val audiometryTaskRepository: AudiometryTaskRepository,
    private val resourceRepository: ResourceRepository,
    private val wordsService: WordsService
) : RecordProcessor<LopotkoRecord, AudiometryTask> {

    val mapHashWord = mutableMapOf<String, String>()

    override fun isApplicable(record: Any): Boolean = record is LopotkoRecord

    @Transactional
    override fun process(records: List<LopotkoRecord>, locale: Locale): List<AudiometryTask> {
        val audiometryTasks = mutableSetOf<AudiometryTask>()

        records.forEach { record ->
            val answerOptions: MutableSet<Resource> = extractAnswerOptions(record, locale)
            resourceRepository.saveAll(answerOptions)

            val audiometryTask = extractAudiometryTask(record, answerOptions)
            val existAudiometryTask = audiometryTaskRepository.findByAudiometryAndFrequencyZoneAndAudiometryGroup(
                audiometryTask.audiometry!!,
                audiometryTask.frequencyZone!!,
                audiometryTask.audiometryGroup!!
            )
            if (existAudiometryTask == null)
                audiometryTasks.add(audiometryTaskRepository.save(audiometryTask))
        }
        wordsService.createTxtFilesWithDiagnosticWords(mapHashWord)
        return audiometryTasks.toMutableList()
    }

    private fun extractAnswerOptions(record: LopotkoRecord, locale: Locale): MutableSet<Resource> {
//      todo: think about voice gender! if (record.exerciseName.startsWith("М")) audioPath = audioPathAlena
        return record.words
            .asSequence()
            .map { it.replace("[()]".toRegex(), StringUtils.EMPTY) }
            .map { toResource(it, locale) }
            .toMutableSet()
    }

    private fun toResource(word: String, locale: Locale): Resource {
        val hashWord = DigestUtils.md5Hex(word)
        mapHashWord[word] = hashWord
        val wordType = WordType.AUDIOMETRY_WORD.toString()
        val audioFileUrl =
            wordsService.getSubFilePathForWord(AudioFileMetaData(word, locale.locale, wordsService.getDefaultManVoiceForLocale(locale.locale)))
        val resource = resourceRepository.findFirstByWordAndWordTypeAndAudioFileUrlLike(word, wordType, audioFileUrl)
            .orElse(
                Resource(
                    word = word,
                    audioFileUrl = audioFileUrl,
                    locale = locale.locale,
                )
            )
        resource.wordType = wordType
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
            answerOptions = answerOptions,
            showSize = 9,
        )
    }
}
