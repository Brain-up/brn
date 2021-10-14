package com.epam.brn.service

import com.epam.brn.config.AwsConfig
import com.epam.brn.exception.ConversionOggToMp3Exception
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.bramp.ffmpeg.FFmpeg
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.FFprobe
import net.bramp.ffmpeg.builder.FFmpegBuilder
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@Service
class AudioFilesGenerationService(
    @Autowired val wordsService: WordsService,
    @Autowired val awsConfig: AwsConfig,
    @Autowired val yandexSpeechKitService: YandexSpeechKitService
) {
    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var folderForLocalFiles: String

    @Value("#{'\${yandex.speeds}'.split(',')}")
    lateinit var speeds: List<String>

    @Value("\${withMp3Conversion}")
    var withMp3Conversion: Boolean = false

    @Value("\${withSavingToS3}")
    var withSavingToS3: Boolean = false

    private val log = logger()

    fun generateAudioFiles() {
        val dictionaryByLocale = wordsService.dictionaryByLocale
        dictionaryByLocale.forEach { (locale, setWordToHash) ->
            val allWords = setWordToHash.keys
            val wordsSize = allWords.size
            if (allWords.isEmpty()) {
                log.info("There are no words at all.")
                return@forEach
            }
            log.info(
                """Start yandex generating audio for locale=${locale.locale} for $wordsSize words. 
                    Need to generate ${wordsSize * speeds.count() * 2}. 
                    Exists=${wordsService.getExistWordFilesCount(locale)}"""
            )
            var createdCounter = AtomicInteger(0)
            var skippedCounter = AtomicInteger(0)
            allWords.asSequence().forEach { word ->
                run {
                    log.info("Start generation audio files for word `$word` speeds:$speeds from $wordsSize words.")
                    speeds.forEach { speed ->
                        GlobalScope.launch {
                            val metaData = AudioFileMetaData(
                                word,
                                locale.locale,
                                wordsService.getDefaultManVoiceForLocale(locale.locale),
                                speed
                            )
                            if (wordsService.isFileExistLocal(metaData)) {
                                log.info("Skipped creation for $metaData.")
                                skippedCounter.incrementAndGet()
                            } else {
                                createdCounter.incrementAndGet()
                                processWord(metaData)
                            }
                        }
                        GlobalScope.launch {
                            val metaData = AudioFileMetaData(
                                word,
                                locale.locale,
                                wordsService.getDefaultWomanVoiceForLocale(locale.locale),
                                speed
                            )
                            if (wordsService.isFileExistLocal(metaData)) {
                                skippedCounter.incrementAndGet()
                            } else {
                                createdCounter.incrementAndGet()
                                processWord(metaData)
                            }
                        }
                    }
                }
            }
            // log.info("Audio files for locale=$locale for ${allWords.size} words were created `$createdCounter`, skipped `$skippedCounter`.")
        }
    }

    /**
     * Generate .ogg audio file from yandex cloud and optionally convert it into .mp3 file and save both of them
     */
    @Transactional
    fun processWord(audioFileMetaData: AudioFileMetaData): File {
        val fileOgg = yandexSpeechKitService.generateAudioOggFile(audioFileMetaData)
        if (withSavingToS3) {
            val subPath = wordsService.getSubPathForWord(audioFileMetaData)
            awsConfig.amazonS3.putObject(awsConfig.bucketName + subPath, fileOgg.name, fileOgg)
            log.info("Aws saving `${audioFileMetaData.text}`: ogg audio file `${fileOgg.name}` saved in S3 ${awsConfig.bucketName + subPath + fileOgg.name}")
        }
        if (withMp3Conversion)
            convertOggFileToMp3(fileOgg, audioFileMetaData.voice.name)
        return fileOgg
    }

    fun convertOggFileToMp3(fileOgg: File, voice: String) {
        val mp3FileName = "${fileOgg.nameWithoutExtension}.mp3"
        val targetMp3FilePath = "$folderForLocalFiles/$voice/$mp3FileName"
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
        log.info("Created mp3 $mp3FileName from ogg.")
        // Or run a two-pass encode (which is better quality at the cost of being slower)
        // executor.createTwoPassJob(builder).run()
    }
}
