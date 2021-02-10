package com.epam.brn.service

import com.epam.brn.enums.Locale
import com.epam.brn.exception.YandexServiceException
import org.apache.commons.codec.digest.DigestUtils
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
import java.io.File
import java.io.InputStream
import java.time.ZonedDateTime

@Service
class YandexSpeechKitService {

    @Value("\${yandex.getTokenLink}")
    lateinit var uriGetIamToken: String

    @Value("\${yandex.authToken}")
    lateinit var authToken: String

    @Value("\${yandex.generationAudioLink}")
    lateinit var uriGenerationAudioFile: String

    @Value("\${yandex.folderId}")
    lateinit var folderId: String

    @Value("\${yandex.lang}")
    lateinit var lang: String

    @Value("\${yandex.voiceFilipp}")
    lateinit var manVoiceRu: String

    @Value("\${yandex.format}")
    lateinit var format: String

    @Value("\${yandex.emotion}")
    lateinit var emotion: String

    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var folderForFiles: String

    var iamToken: String = ""
    var iamTokenExpiresTime = ZonedDateTime.now()

    private val log = logger()

    private val mapLocaleVoice =
        mapOf(Locale.RU.locale to "filipp", Locale.EN.locale to "nick", Locale.TR.locale to "erkanyavas")

    fun getYandexIamTokenForAudioGeneration(): String {
        if (iamToken.isNotEmpty() && iamTokenExpiresTime.isAfter(ZonedDateTime.now()))
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
        iamTokenExpiresTime = ZonedDateTime.now().plusHours(11)
        log.info("Get iam token from yandex cloud successfully, it will expire at $tokenExpiresTimeValue")
        return iamToken
    }

    /**
     * Generate stream of .ogg audio file from yandex cloud speech kit service
     */
    fun generateAudioStream(
        word: String,
        voice: String = this.manVoiceRu,
        speed: String = "1",
        lang: String = this.lang
    ): InputStream {
        val token = getYandexIamTokenForAudioGeneration()
        val parameters = ArrayList<NameValuePair>().apply {
            add(BasicNameValuePair("folderId", folderId))
            add(BasicNameValuePair("lang", lang))
            add(BasicNameValuePair("format", format))
            add(BasicNameValuePair("voice", voice))
            add(BasicNameValuePair("emotion", emotion))
            add(BasicNameValuePair("speed", speed))
            add(BasicNameValuePair("text", word))
        }

        val uriBuilder = URIBuilder(uriGenerationAudioFile)
        uriBuilder.addParameters(parameters)

        val postRequest = HttpPost(uriBuilder.build())
        postRequest.setHeader("Authorization", "Bearer $token")

        val httpClient = HttpClientBuilder.create().build()
        val response = httpClient.execute(postRequest)
        val statusCode = response.statusLine.statusCode
        if (statusCode != HttpStatus.OK.value())
            throw YandexServiceException("Yandex cloud does not provide audio file for word `$word`, httpStatus={$statusCode}")
        log.info("Ogg audio file for Word `$word` was successfully generated.")
        val httpEntity = response.entity
        return httpEntity.content
    }

    /**
     * Generate .ogg audio file from yandex cloud speech kit service if it is absent locally
     */
    fun generateAudioOggFile(
        word: String,
        voice: String,
        speed: String = "1",
        lang: String = Locale.RU.locale
    ): File {
        val md5Hash = DigestUtils.md5Hex(word)
        log.info("For word `$word` is created audio file with name `$md5Hash.ogg`")
        val fileOgg = File("$md5Hash.ogg")
        val targetOggFile =
            if (speed == "1")
                File("$folderForFiles/ogg/$voice/${fileOgg.name}")
            else
                File("$folderForFiles/ogg/$voice/$speed/${fileOgg.name}")
        if (targetOggFile.exists()) {
            log.info("${fileOgg.name} is already exist, generation was skipped.")
            return targetOggFile
        }

        val inputStream = generateAudioStream(word, voice, speed, lang)

        FileUtils.copyInputStreamToFile(inputStream, fileOgg)
        fileOgg.let { sourceFile ->
            sourceFile.copyTo(targetOggFile, true)
            sourceFile.delete()
        }
        return targetOggFile
    }

    fun validateLocale(locale: String) {
        if (!Locale.values().map { it.locale }.contains(locale.toLowerCase()))
            throw IllegalArgumentException("Locale $locale does not support yet for generation audio files.")
    }

    fun generateAudioOggFileWithValidation(word: String, locale: String): InputStream {
        validateLocale(locale)
        return generateAudioStream(word = word, voice = mapLocaleVoice.getValue(locale), lang = locale)
    }
}
