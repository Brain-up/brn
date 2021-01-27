package com.epam.brn.service

import com.epam.brn.config.AwsConfig
import com.epam.brn.exception.ConversionOggToMp3Exception
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.apache.commons.codec.digest.DigestUtils
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

@Service
class AudioFilesGenerationService(
    @Autowired val wordsService: WordsService,
    @Autowired val awsConfig: AwsConfig,
    @Autowired val yandexSpeechKitService: YandexSpeechKitService
) {
    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var folderForFiles: String

    @Value("\${yandex.voiceFilipp}")
    lateinit var manVoice: String

    @Value("\${yandex.voiceAlena}")
    lateinit var womanVoice: String

    @Value("#{'\${yandex.speeds}'.split(',')}")
    lateinit var speeds: List<String>

    @Value("\${withMp3Conversion}")
    var withMp3Conversion: Boolean = false

    @Value("\${withSavingToS3}")
    var withSavingToS3: Boolean = false

    private val log = logger()

    fun generateAudioFiles() {
        val allWords = wordsService.fullWordsSet
        val existsHashWords = wordsService.getExistWordFiles()
        val wordsSize = allWords.size
        if (allWords.isEmpty()) {
            log.info("There are no any words.")
            return
        }
        log.info("Start generating audio files in yandex cloud for $wordsSize words. exists=${existsHashWords.size}")
        var createdCounter = 0
        var skippedCounter = 0
        allWords.asSequence().forEach { word ->
            run {
                val md5Hash = DigestUtils.md5Hex(word)
                if (!existsHashWords.contains(md5Hash)) {
                    createdCounter++
                    log.info("Generate $createdCounter word `$word` speeds:$speeds from $wordsSize words.")
                    speeds.forEach {
                        GlobalScope.launch { processWord(word, womanVoice, it) }
                        GlobalScope.launch { processWord(word, manVoice, it) }
                    }
                } else
                    skippedCounter++
            }
        }
        log.info("Audio files for ${allWords.size} words were created `$createdCounter`, skipped `$skippedCounter`.")
    }

    /**
     * Generate .ogg audio file from yandex cloud and optionally convert it into .mp3 file and save both of them
     */
    fun processWord(word: String, voice: String, speed: String): File {
        val fileOgg = yandexSpeechKitService.generateAudioOggFile(word, voice, speed)
        if (withSavingToS3)
            awsConfig.amazonS3.putObject(awsConfig.bucketName + "/audio/$voice", fileOgg.name, fileOgg)
        log.info("Ogg audio file `${fileOgg.name}` was successfully save in S3 ${awsConfig.bucketName + "/audio/$voice/" + fileOgg.name}.")
        if (withMp3Conversion)
            convertOggFileToMp3(fileOgg, voice)
        return fileOgg
    }

    fun convertOggFileToMp3(fileOgg: File, voice: String) {
        val mp3FileName = "${fileOgg.nameWithoutExtension}.mp3"
        val targetMp3FilePath = "$folderForFiles/$voice/$mp3FileName"
        val fileMp3 = File(targetMp3FilePath)
        if (fileMp3.exists()) {
            log.info("$mp3FileName is already exist, it was not replaced, it was skipped.")
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
}
