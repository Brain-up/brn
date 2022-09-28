package com.epam.brn.service.load

import com.epam.brn.enums.AudiometryType
import com.epam.brn.enums.EAR
import com.epam.brn.enums.BrnLocale
import com.epam.brn.model.Audiometry
import com.epam.brn.model.AudiometryTask
import com.epam.brn.repo.AudiometryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AudiometryLoader(
    private val audiometryRepository: AudiometryRepository,
    private val audiometryTaskRepository: AudiometryTaskRepository
) {
    private val log = logger()

    @Value("#{'\${frequencyForDiagnostic}'.split(',')}")
    lateinit var frequencyForDiagnostic: List<Int>

    @Value("\${createOrUpdate}")
    var createOrUpdate: Boolean = false

    fun loadInitialAudiometricsWithTasks() {
        val audiometrySignal = Audiometry(
            locale = BrnLocale.RU.locale,
            name = "Частотная диагностика",
            description = "Частотная диагностика",
            audiometryType = AudiometryType.SIGNALS.name
        )
        val audiometrySpeech = Audiometry(
            locale = BrnLocale.RU.locale,
            name = "Речевая диагностика",
            description = "Речевая диагностика методом Лопотко",
            audiometryType = AudiometryType.SPEECH.name
        )
        val audiometryMatrix = Audiometry(
            locale = BrnLocale.RU.locale,
            name = "Матриксная диагностика",
            description = "Матриксная диагностика",
            audiometryType = AudiometryType.MATRIX.name
        )
        val audiometrySignalEn = Audiometry(
            locale = BrnLocale.EN.locale,
            name = "Frequency diagnostic",
            description = "Frequency diagnostic",
            audiometryType = AudiometryType.SIGNALS.name
        )
        val audiometrySpeechEn = Audiometry(
            locale = BrnLocale.EN.locale,
            name = "Speech diagnostic",
            description = "Speech diagnostic with Lopotko words sequences",
            audiometryType = AudiometryType.SPEECH.name
        )
        val audiometryMatrixEn = Audiometry(
            locale = BrnLocale.EN.locale,
            name = "Matrix diagnostic",
            description = "Matrix diagnostic",
            audiometryType = AudiometryType.MATRIX.name
        )
        saveAudiometrics(
            listOf(
                audiometrySignal,
                audiometrySpeech,
                audiometryMatrix,
                audiometrySignalEn,
                audiometrySpeechEn,
                audiometryMatrixEn
            )
        )
        loadFrequencyDiagnosticData()
    }

    fun saveAudiometrics(list: List<Audiometry>) {
        if (createOrUpdate)
            list.forEach { createOrUpdate(it) }
        else
            audiometryRepository.saveAll(list)
    }

    fun createOrUpdate(audiometry: Audiometry): Audiometry {
        val existAudiometry =
            audiometryRepository.findByAudiometryTypeAndLocale(audiometry.audiometryType, audiometry.locale)
        return if (existAudiometry == null)
            audiometryRepository.save(audiometry) // create
        else {
            existAudiometry.description = audiometry.description
            audiometryRepository.save(existAudiometry) // update existing audiometry
        }
    }

    fun loadFrequencyDiagnosticData() {
        val audiometrics = audiometryRepository.findByAudiometryType(AudiometryType.SIGNALS.name)
        audiometrics.forEach { audiometry ->
            val existTasksCount = audiometryTaskRepository.countByAudiometry(audiometry)
            if (existTasksCount == 2L)
                return@forEach
            if (existTasksCount != 0L) {
                log.error("Wrong db status: for audiometry $audiometry existTasksCount=$existTasksCount")
                audiometryTaskRepository.removeAllByAudiometry(audiometry) // fix db bug
            }
            val taskLeft =
                AudiometryTask(
                    audiometry = audiometry,
                    ear = EAR.LEFT.name,
                    frequencies = frequencyForDiagnostic.toString()
                )
            val taskRight =
                AudiometryTask(
                    audiometry = audiometry,
                    ear = EAR.RIGHT.name,
                    frequencies = frequencyForDiagnostic.toString()
                )
            audiometryTaskRepository.saveAll(listOf(taskLeft, taskRight))
        }
    }
}
