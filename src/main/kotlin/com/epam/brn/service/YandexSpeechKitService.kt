package com.epam.brn.service

import com.epam.brn.enums.Locale
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
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class YandexSpeechKitService(
    private val wordsService: WordsService,
    private val timeService: TimeService
) {

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
            add(BasicNameValuePair("voice", audioFileMetaData.voice.name.toLowerCase()))
            add(BasicNameValuePair("emotion", emotion))
            add(BasicNameValuePair("speed", audioFileMetaData.speed))
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
    fun generateAudioOggFile(audioFileMetaData: AudioFileMetaData): File {
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

    fun validateLocale(locale: String) {
        if (!Locale.values().map { it.locale }.contains(locale.toLowerCase()))
            throw IllegalArgumentException("Locale $locale does not support yet for generation audio files.")
    }

    fun generateAudioOggFileWithValidation(text: String, locale: String): InputStream {
        validateLocale(locale)
        return generateAudioStream(
            AudioFileMetaData(
                text,
                locale,
                wordsService.getDefaultWomanVoiceForLocale(locale)
            )
        )
    }
}
