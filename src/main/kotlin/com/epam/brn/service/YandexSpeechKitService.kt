package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.BrnLocale
import com.epam.brn.exception.YandexServiceException
import org.apache.commons.io.FileUtils
import org.apache.http.NameValuePair
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.apache.logging.log4j.kotlin.logger
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
@Primary
@ConditionalOnProperty(name = ["default.tts.provider"], havingValue = "yandex")
class YandexSpeechKitService(
    private val wordsService: WordsService,
    private val timeService: TimeService
) : TextToSpeechService {

    @Value("\${yandex.getTokenLink}")
    lateinit var uriGetIamToken: String

    @Value("\${yandex.authToken}")
    lateinit var authToken: String

    @Value("\${yandex.generationAudioLink}")
    lateinit var uriGenerationAudioFile: String

    @Value("\${yandex.folderId}")
    lateinit var folderId: String

    @Value("\${yandex.format}")
    lateinit var format: String

    @Value("\${yandex.emotion}")
    lateinit var emotion: String

    var iamToken: String = ""
    var iamTokenExpiresTime: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

    val setNotCreationWords = mutableSetOf<AudioFileMetaData>()

    private val log = logger()

    @Transactional
    fun getYandexIamTokenForAudioGeneration(): String {
        if (iamToken.isNotEmpty() && iamTokenExpiresTime.isAfter(timeService.now()))
            return iamToken
        val parameters = ArrayList<NameValuePair>()
        parameters.add(BasicNameValuePair("yandexPassportOauthToken", authToken))
        val uriBuilder = URIBuilder(uriGetIamToken)
        uriBuilder.addParameters(parameters)
        val postRequest = HttpPost(uriBuilder.build())
        val httpClient = HttpClientBuilder.create().build()
        val response: CloseableHttpResponse = httpClient.execute(postRequest)
        val statusCode = response.statusLine.statusCode
        if (statusCode != HttpStatus.OK.value())
            throw YandexServiceException("Can't get yandex iam token, httpStatus={$statusCode}")
        val entity = EntityUtils.toString(response.entity)
        val jsonObject = JSONObject(entity)
        iamToken = jsonObject.getString("iamToken")
        val tokenExpiresTimeValue = jsonObject.getString("expiresAt")
        iamTokenExpiresTime = timeService.now()
        log.info("Get iam token from yandex cloud successfully, it will expire at $tokenExpiresTimeValue")
        return iamToken
    }

    /**
     * Generate stream of .ogg audio file from yandex cloud speech kit service
     */
    @Transactional
    fun generateAudioStream(audioFileMetaData: AudioFileMetaData): InputStream {
        val token = getYandexIamTokenForAudioGeneration()
        val parameters = ArrayList<NameValuePair>().apply {
            add(BasicNameValuePair("folderId", folderId))
            add(BasicNameValuePair("lang", audioFileMetaData.locale))
            add(BasicNameValuePair("format", format))
            add(BasicNameValuePair("voice", audioFileMetaData.voice.toLowerCase()))
            add(BasicNameValuePair("emotion", emotion))
            add(BasicNameValuePair("speed", audioFileMetaData.speedFloat))
            add(BasicNameValuePair("text", audioFileMetaData.text))
        }

        val uriBuilder = URIBuilder(uriGenerationAudioFile)
        uriBuilder.addParameters(parameters)

        val postRequest = HttpPost(uriBuilder.build())
        postRequest.setHeader("Authorization", "Bearer $token")

        val httpClient = HttpClientBuilder.create().build()
        val response = httpClient.execute(postRequest)
        var count = 10
        var success = false
        var statusCode = 0
        while (!success) {
            if (count == 0) break
            count--
            statusCode = response.statusLine.statusCode
            if (statusCode != HttpStatus.OK.value()) log.error("====== for $audioFileMetaData, httpStatus={$statusCode}, count=$count ======")
            else success = true
        }
        if (statusCode != HttpStatus.OK.value()) {
            setNotCreationWords.add(audioFileMetaData)
            throw YandexServiceException("Yandex cloud does not provide audio file for $audioFileMetaData, httpStatus={$statusCode}, content=${response.entity.content}")
        }
        log.info("Ogg audio file for $audioFileMetaData was successfully generated by yandex!")
        val httpEntity = response.entity
        return httpEntity.content
    }

    /**
     * Generate .ogg audio file from yandex cloud speech kit service if it is absent locally
     */
    @Transactional
    override fun generateAudioOggFile(audioFileMetaData: AudioFileMetaData): File {
        val fileName = wordsService.getLocalFilePathForWord(audioFileMetaData)
        log.info("For word $audioFileMetaData started creation audio file with name `$fileName`")
        val fileOgg = File(fileName)
        if (fileOgg.exists()) {
            log.info("File ${fileOgg.name} is already exist, generation was skipped.")
            return fileOgg
        }
        val inputStream = generateAudioStream(audioFileMetaData)
        FileUtils.copyInputStreamToFile(inputStream, fileOgg)
        log.info("File for $audioFileMetaData was created: $fileName")
        return fileOgg
    }

    fun validateLocaleAndVoice(locale: String, voice: String) {
        if (!BrnLocale.values().map { it.locale }.contains(locale.toLowerCase()))
            throw IllegalArgumentException("Locale $locale does not support yet for generation audio files.")
        val localeVoices = wordsService.getVoicesForLocale(locale)
        if (voice.isNotEmpty() && !localeVoices.contains(voice))
            throw IllegalArgumentException("Locale $locale does not support voice $voice, only $localeVoices.")
    }

    override fun generateAudioOggFileWithValidation(audioFileMetaData: AudioFileMetaData): InputStream {
        validateLocaleAndVoice(audioFileMetaData.locale, audioFileMetaData.voice)
        return generateAudioStream(
            AudioFileMetaData(
                audioFileMetaData.text,
                audioFileMetaData.locale,
                audioFileMetaData.voice.ifEmpty { wordsService.getDefaultWomanVoiceForLocale(audioFileMetaData.locale) },
                audioFileMetaData.speedFloat,
            )
        )
    }
}
