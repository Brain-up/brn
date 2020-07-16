package com.epam.brn.service

import com.epam.brn.exception.ConversionOggToMp3Exception
import com.epam.brn.exception.YandexServiceException
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.NameValuePair
import java.util.ArrayList
import org.apache.http.client.utils.URIBuilder
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.builder.FFmpegBuilder
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.FFmpeg
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils
import org.apache.logging.log4j.kotlin.logger
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import java.time.ZonedDateTime

@Service
class AudioFilesGenerationService(@Autowired val wordsService: WordsService) {

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

    @Value("\${yandex.format}")
    lateinit var format: String

    @Value("\${yandex.voiceFilipp}")
    lateinit var voiceFilipp: String

    @Value("\${yandex.voiceAlena}")
    lateinit var voiceAlena: String

    @Value("\${yandex.emotion}")
    lateinit var emotion: String

    @Value(value = "\${series1WordsFileName}")
    private lateinit var series1WordsFileName: String

    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var folderForFiles: String

    var iamToken: String = ""
    var iamTokenExpiresTime = ZonedDateTime.now()

    private val log = logger()

    fun generateAudioFiles() {
        val words = wordsService.wordsSet
        val wordsSize = words.size
        words.remove("")
        if (words.isEmpty())
            log.error("There are no any cached words.")
        log.info("Start generating audio files in yandex cloud for $wordsSize words.")
        var counter = 1
        words.asSequence().forEach { word ->
            run {
                log.info("Generated $counter word from $wordsSize words.")
                generateAudioFile(word, voiceAlena)
                generateAudioFile(word, voiceFilipp)
                counter += 1
            }
        }
        log.info("Audio files for all words (${words.size}) was created successfully!")
    }

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

    fun generateAudioFile(word: String, voice: String): File {
        val token = getYandexIamTokenForAudioGeneration()
        val parameters = ArrayList<NameValuePair>()
        parameters.add(BasicNameValuePair("folderId", folderId))
        parameters.add(BasicNameValuePair("lang", lang))
        parameters.add(BasicNameValuePair("format", format))
        parameters.add(BasicNameValuePair("voice", voice))
        parameters.add(BasicNameValuePair("emotion", emotion))
        parameters.add(BasicNameValuePair("text", word))

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
        val inputStream = httpEntity.content
        val fileOgg = File("$word.ogg")
        FileUtils.copyInputStreamToFile(inputStream, fileOgg)

        convertOggFileToMp3(fileOgg, voice)

        val targetOggFile = File("$folderForFiles/ogg/$voice/${fileOgg.name}")
        fileOgg.let { sourceFile ->
            sourceFile.copyTo(targetOggFile, true)
            sourceFile.delete()
        }
        return targetOggFile
    }

    fun convertOggFileToMp3(fileOgg: File, voice: String) {
        val mp3FileName = "${fileOgg.nameWithoutExtension}.mp3"
        try {
            val builder = FFmpegBuilder()
                .setInput(fileOgg.getAbsolutePath())
                .overrideOutputFiles(true)
                .addOutput(mp3FileName)
                .setAudioCodec("libmp3lame")
                .setAudioChannels(FFmpeg.AUDIO_MONO)
                .setAudioBitRate(48000)
                .setAudioSampleRate(FFmpeg.AUDIO_SAMPLE_16000)
                .done()

            val ffmpeg = FFmpeg("ffmpeg/bin/ffmpeg.exe")
            val ffprobe = FFprobe("ffmpeg/bin/ffprobe.exe")
            val executor = FFmpegExecutor(ffmpeg, ffprobe)

            // Run a one-pass encode
            executor.createJob(builder).run()

            val targetMp3FilePath = "$folderForFiles/$voice/$mp3FileName"
            File(mp3FileName).let { sourceFile ->
                sourceFile.copyTo(File(targetMp3FilePath), true)
                sourceFile.delete()
            }
        } catch (e: Exception) {
            throw ConversionOggToMp3Exception("Some exception was appeared in converting process: ${e.message}")
        }
        // Or run a two-pass encode (which is better quality at the cost of being slower)
        // executor.createTwoPassJob(builder).run()
    }
}
