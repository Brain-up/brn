package com.epam.brn.service

import com.epam.brn.config.AwsConfig
import com.epam.brn.exception.ConversionOggToMp3Exception
import com.epam.brn.exception.YandexServiceException
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.File
import java.time.ZonedDateTime

@Service
class AudioFilesGenerationService(
    @Autowired val wordsService: WordsService,
    @Autowired val awsConfig: AwsConfig
) {

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

    @Value("\${yandex.speed}")
    lateinit var speed: String

    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var folderForFiles: String

    var iamToken: String = ""
    var iamTokenExpiresTime = ZonedDateTime.now()

    private val log = logger()

    fun generateAudioFiles() {
        val allWords = wordsService.fullWordsSet
        val existsHashWords = wordsService.existsFileNames
        val wordsSize = allWords.size
        if (allWords.isEmpty()) {
            log.info("There are no any words.")
            return
        }
        log.info("Start generating audio files in yandex cloud for $wordsSize words. exists=${existsHashWords.size}")
        var counter = 1
        allWords.asSequence().forEach { word ->
            run {
                val md5Hash = DigestUtils.md5Hex(word)
                if (!existsHashWords.contains(md5Hash)) {
                    log.info("Generated $counter word from $wordsSize words.")
                    generateAudioFiles(word, voiceAlena)
                    generateAudioFiles(word, voiceFilipp)
                    counter += 1
                }
            }
        }
        log.info("Audio files for all words (${allWords.size}) was created successfully!")
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

    /**
     * Generate .ogg audio file from yandex cloud and convert it into .mp3 file and save both of them
     */
    fun generateAudioFiles(word: String, voice: String): File {
        val md5Hash = DigestUtils.md5Hex(word)
        log.info("For word `$word` is created audio file with name `$md5Hash.ogg`")
        val fileOgg = File("$md5Hash.ogg")
        val targetOggFile = File("$folderForFiles/ogg/$voice/${fileOgg.name}")
        if (targetOggFile.exists()) {
            log.info("${fileOgg.name} is already exist, it was not generated, it was skipped.")
            return targetOggFile
        }
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
        val inputStream = httpEntity.content
        FileUtils.copyInputStreamToFile(inputStream, fileOgg)

        awsConfig.amazonS3.putObject(awsConfig.bucketName + "/audio/$voice", fileOgg.name, fileOgg)
        log.info("Ogg audio file `${fileOgg.name}` was successfully save in S3 ${awsConfig.bucketName + "/audio/$voice/" + fileOgg.name}.")

        convertOggFileToMp3(fileOgg, voice)

        fileOgg.let { sourceFile ->
            sourceFile.copyTo(targetOggFile, true)
            sourceFile.delete()
        }
        return targetOggFile
    }

    fun convertOggFileToMp3(fileOgg: File, voice: String) {
        val mp3FileName = "${fileOgg.nameWithoutExtension}.mp3"
        val targetMp3FilePath = "$folderForFiles/$voice/$mp3FileName"
        val fileMp3 = File(targetMp3FilePath)
        if (fileMp3.exists()) {
            log.info("$mp3FileName is already exist, it was not rewrited, it was skipped.")
            return
        }

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

    fun generateOggFile(word: String, voice: String): File {
        val token = getYandexIamTokenForAudioGeneration()
        val restTemplate = RestTemplate()
        val parameters = ArrayList<NameValuePair>()
        parameters.add(BasicNameValuePair("folderId", folderId))
        parameters.add(BasicNameValuePair("lang", lang))
        parameters.add(BasicNameValuePair("format", format))
        parameters.add(BasicNameValuePair("voice", voice))
        parameters.add(BasicNameValuePair("emotion", emotion))
        parameters.add(BasicNameValuePair("text", word))
        val uriBuilder = URIBuilder(uriGenerationAudioFile)
        uriBuilder.addParameters(parameters)

        val headers = HttpHeaders()
        headers.setBearerAuth("$token")

        val request = HttpEntity(null, headers)

        val response: ResponseEntity<ByteArray> = restTemplate
            .exchange(uriBuilder.build(), HttpMethod.POST, request, ByteArray::class.java)
        if (response.statusCode != HttpStatus.OK)
            throw YandexServiceException("Yandex cloud does not provide audio file for word `$word`, httpStatus={${response.statusCode}}")
        log.info("Ogg audio file for Word `$word` was successfully generated.")

        val byteArray = response.body!!
        val fileOgg = File("$word.ogg")
        FileUtils.writeByteArrayToFile(fileOgg, byteArray)
        return fileOgg
    }
}
