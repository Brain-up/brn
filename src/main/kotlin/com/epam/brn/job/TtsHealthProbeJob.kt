package com.epam.brn.job

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.Voice
import com.epam.brn.service.TextToSpeechService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Periodically synthesizes a tiny phrase through the active text-to-speech
 * provider so an outage (e.g. a revoked/expired Yandex API key) is detected
 * proactively, without waiting for a real user request.
 *
 * The probe does not alert directly: a provider failure surfaces as an exception
 * from the provider, which already sends the operational alert. The probe simply
 * generates traffic and swallows the failure so the scheduler keeps running.
 */
@Component
@ConditionalOnProperty(name = ["tts.probe.enabled"], havingValue = "true")
class TtsHealthProbeJob(
    private val textToSpeechService: TextToSpeechService,
) {
    private val log = logger()

    @Scheduled(cron = "\${tts.probe.cron}")
    fun probe() {
        val meta = AudioFileMetaData("проверка связи", BrnLocale.RU.locale, Voice.OKSANA.name, "1")
        try {
            textToSpeechService.generateAudioOggStreamWithValidation(meta).use { /* consume and close */ }
            log.debug("TTS health probe succeeded")
        } catch (e: Exception) {
            log.error("TTS health probe failed: ${e.message}", e)
        }
    }
}
