package com.epam.brn.service.azure.tts

import com.epam.brn.config.ExcludeFromJacocoGeneratedReport
import com.epam.brn.dto.azure.tts.AzureJsonVoiceInfo
import com.epam.brn.dto.azure.tts.AzurePitches
import com.epam.brn.dto.azure.tts.AzureRates
import com.epam.brn.dto.azure.tts.AzureTextToSpeechRequest
import com.epam.brn.dto.azure.tts.ExpressAs
import com.epam.brn.dto.azure.tts.Prosody
import com.epam.brn.dto.azure.tts.Voice
import com.epam.brn.exception.AzureTtsException
import com.epam.brn.model.azure.tts.AzureVoiceInfo
import com.epam.brn.repo.azure.tts.AzureVoiceInfoRepository
import com.epam.brn.service.AudioFileMetaData
import com.epam.brn.service.TextToSpeechService
import com.epam.brn.service.WordsService
import com.epam.brn.service.azure.tts.config.AzureTtsProperties
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.apache.commons.io.FileUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.io.File
import java.io.InputStream
import java.time.Duration

@Service
@ConditionalOnProperty(name = ["default.tts.provider"], havingValue = "azure")
class AzureTextToSpeechService(
    private val wordsService: WordsService,
    private val azureTtsWebClient: WebClient,
    private val azureAllVoicesWebClient: WebClient,
    private val azureVoiceRepo: AzureVoiceInfoRepository,
    private val azureTtsProperties: AzureTtsProperties
) : TextToSpeechService {
    val log = logger()

    /**
     * Makes call to Azure TTS service to get InputStream with audio by provided parameters
     */
    @ExcludeFromJacocoGeneratedReport
    fun textToSpeech(params: TextToSpeechParams): InputStream {
        val textToSpeechRequest = getTextToSpeechRequest(params)
        return azureTtsWebClient.post()
            .headers { headers ->
                headers.set("Ocp-Apim-Subscription-Key", azureTtsProperties.ocpApimSubscriptionKey)
                headers.set("X-Microsoft-OutputFormat", azureTtsProperties.defaultOutputFormat)
                headers.set("Content-type", "application/ssml+xml")
            }
            .bodyValue(textToSpeechRequest)
            .retrieve()
            .bodyToMono(InputStreamResource::class.java)
            .doOnError { e -> log.error("Error while processing Azure text-to-speech request, \nerror: {}", e) }
            .map { it.inputStream }
            .block(Duration.ofSeconds(15))
            ?: throw AzureTtsException("Azure TTS does not provide audio file for $textToSpeechRequest")
    }

    /**
     * Receives all available voices for Azure TTS service
     */
    @ExcludeFromJacocoGeneratedReport
    fun getVoices(): List<AzureJsonVoiceInfo> {
        return azureAllVoicesWebClient.get()
            .headers { headers ->
                headers.set("Ocp-Apim-Subscription-Key", azureTtsProperties.ocpApimSubscriptionKey)
            }
            .retrieve()
            .bodyToFlux(AzureJsonVoiceInfo::class.java)
            .collectList()
            .doOnError { e -> log.error("Error while getting Azure voices, \nerror: {}", e) }
            .block(Duration.ofSeconds(15))
            ?: throw AzureTtsException("Azure TTS does not provide voices")
    }

    @ExcludeFromJacocoGeneratedReport
    override fun generateAudioOggFile(audioFileMetaData: AudioFileMetaData): File {
        val fileName = wordsService.getLocalFilePathForWord(audioFileMetaData)
        log.info("For word $audioFileMetaData started creation audio file with name `$fileName`")
        val fileOgg = File(fileName)
        if (fileOgg.exists()) {
            log.info("File ${fileOgg.name} is already exist, generation was skipped.")
            return fileOgg
        }
        val inputStream = textToSpeech(
            TextToSpeechParams(
                text = audioFileMetaData.text,
                locale = audioFileMetaData.locale,
                voice = audioFileMetaData.voice.name
            )
        )
        FileUtils.copyInputStreamToFile(inputStream, fileOgg)
        log.info("File for $audioFileMetaData was created: $fileName")
        return fileOgg
    }

    /**
     * Makes call to Azure Text To Speech service to get audio file for provided text and params
     *
     * See [getVoiceInfo] description to understand how voice choosing works
     *
     * @param text Text that should be transformed into speech
     * @param locale Accepted locales located in `azure.tts.accepted-locales` property
     * @param voice [AzureVoiceInfo.shortName]
     * @param speed See description in [AzureRates]
     * @param gender Male / Female
     * @param pitch See description in [AzurePitches]
     * @param style Closely related to chosen voice. See azure_speech_style table [AzureVoiceInfo.styleList]
     */
    override fun generateAudioOggFileWithValidation(
        text: String,
        locale: String,
        voice: String,
        speed: String,
        gender: String?,
        pitch: String?,
        style: String?
    ): InputStream =
        textToSpeech(
            TextToSpeechParams(
                text = text,
                locale = locale,
                voice = voice,
                gender = gender,
                speed = speed,
                pitch = pitch,
                style = style
            )
        )

    /**
     * Creates XML request to Azure TTS service
     */
    fun getTextToSpeechRequest(params: TextToSpeechParams): String {
        val voiceInfo = getVoiceInfo(params)

        return XmlMapper().writeValueAsString(
            AzureTextToSpeechRequest(
                voice = Voice(
                    name = voiceInfo.shortName,
                    gender = voiceInfo.gender,
                    lang = voiceInfo.locale,
                    prosody = Prosody(
                        pitch = defaultIfBlank(params.pitch, AzurePitches.DEFAULT.code),
                        rate = defaultIfBlank(params.speed, AzureRates.DEFAULT.code),
                        expressAs = ExpressAs(
                            style = voiceInfo.styleList.find { it.name == params.style }?.name,
                            styledegree = params.styledegree,
                            text = params.text
                        )
                    ),
                ),
                lang = voiceInfo.locale,
            )
        )
    }

    /**
     * Priorities of getting voice:
     *  1. by shortName from DB
     *  2. by locale + gender from DB (gender is optional, by default it's azure.tts.default-gender)
     *  3. from properties
     */
    fun getVoiceInfo(params: TextToSpeechParams): AzureVoiceInfo =
        defaultIfBlank(params.voice, null)
            ?.let { azureVoiceRepo.findByShortName(it) }
            ?: getVoiceInfoByLocalAndGender(params.locale, params.gender)
            ?: AzureVoiceInfo(
                shortName = azureTtsProperties.defaultVoiceName,
                gender = azureTtsProperties.defaultGender,
                locale = azureTtsProperties.defaultLang
            )

    private fun getVoiceInfoByLocalAndGender(locale: String, gender: String?) =
        azureVoiceRepo.findByLocaleIgnoreCaseAndGenderIgnoreCase(locale, gender ?: azureTtsProperties.defaultGender)
            .firstOrNull()

    private fun defaultIfBlank(text: String?, defaultValue: String?): String? =
        if (text == null || text.isBlank()) defaultValue else text

    data class TextToSpeechParams(
        val text: String,
        val locale: String,
        val voice: String,
        val gender: String? = null,
        val speed: String? = null,
        val pitch: String? = null,
        val style: String? = null,
        val styledegree: String = "1"
    )
}
