package com.epam.brn.service.azure.tts

import com.epam.brn.model.azure.tts.AzureSpeechStyle
import com.epam.brn.model.azure.tts.AzureVoiceInfo
import com.epam.brn.repo.azure.tts.AzureVoiceInfoRepository
import com.epam.brn.service.azure.tts.config.AzureTtsProperties
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Makes request to Azure TTS service to get all available voices.
 * Filters received voices by accepted locales (azure.tts.accepted-locales) and saves it into DB.
 * This loader makes call to Azure only if Azure voice table is empty (azure_voice_info, [AzureVoiceInfo]).
 */
@Component
@Profile("dev", "prod")
@ConditionalOnProperty(name = ["default.tts.provider"], havingValue = "azure")
class AzureVoiceLoader(
    private val azureTtsService: AzureTextToSpeechService,
    private val azureVoiceRepo: AzureVoiceInfoRepository,
    private val azureTtsProperties: AzureTtsProperties,
) : CommandLineRunner {
    val log = logger()

    override fun run(vararg args: String?) {
        val countOfVoicesInDb = azureVoiceRepo.count()
        if (countOfVoicesInDb > 0) {
            log.info("Azure voice info table already filled")
            return
        }
        val jsonVoices =
            azureTtsService
                .getVoices()
                .filter { azureTtsProperties.acceptedLocales.contains(it.locale) }
        val allStyles =
            jsonVoices
                .flatMap { it.styleList ?: emptyList() }
                .distinct()
                .map { AzureSpeechStyle(name = it) }
                .toMutableSet()

        val voices =
            jsonVoices.map { voice ->
                voice.convertToEntity(
                    allStyles.filter { style -> voice.styleList?.contains(style.name) ?: false }.toMutableSet(),
                )
            }
        log.info("Filling Azure voices table. Found [${voices.count()}] voices and [${allStyles.size} styles]")
        azureVoiceRepo.saveAll(voices)
    }
}
