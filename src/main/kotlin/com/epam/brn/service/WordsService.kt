package com.epam.brn.service

import com.epam.brn.enums.Locale
import com.epam.brn.enums.Voice
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import javax.annotation.PostConstruct

@Service
class WordsService {

    @Value(value = "\${wordsFileNameRu}")
    private lateinit var wordsFileNameRu: String

    @Value(value = "\${wordsFileNameEn}")
    private lateinit var wordsFileNameEn: String

    @Value(value = "\${lopotkoFileName}")
    private lateinit var lopotkoFileName: String

    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var localFolderForFiles: String

    @Value("\${aws.baseFileUrl}")
    private lateinit var baseFileUrl: String

    @Value(value = "\${brn.audio.file.series.path}")
    private lateinit var audioFileSeriesPath: String

    private val mapLocaleManVoice =
        mapOf(Locale.RU.locale to Voice.FILIPP, Locale.EN.locale to Voice.NICK, Locale.TR.locale to Voice.ERKANYAVAS)

    private val mapLocaleWomanVoice =
        mapOf(Locale.RU.locale to Voice.OKSANA, Locale.EN.locale to Voice.ALYSS, Locale.TR.locale to Voice.SILAERKAN)

    fun getDefaultManVoiceForLocale(locale: String): Voice = mapLocaleManVoice[locale]!!
    fun getDefaultWomanVoiceForLocale(locale: String): Voice = mapLocaleWomanVoice[locale]!!

    val dictionaryByLocale =
        mutableMapOf(Locale.RU to mutableMapOf<String, String>(), Locale.EN to mutableMapOf<String, String>())

    lateinit var mapLocaleFile: Map<Locale, String>

    @PostConstruct
    private fun init() {
        mapLocaleFile = mapOf(Locale.RU to wordsFileNameRu, Locale.EN to wordsFileNameEn)
    }

    fun addWordToDictionary(locale: Locale, word: String, wordHash: String) =
        dictionaryByLocale[locale]!!.put(word, wordHash)

    fun addWordToDictionary(locale: Locale, word: String) =
        dictionaryByLocale[locale]!!.put(word, DigestUtils.md5Hex(word))

    fun addWordsToDictionary(locale: Locale, words: Collection<String>) =
        words.forEach { dictionaryByLocale[locale]!![it] = DigestUtils.md5Hex(it) }

    fun createTxtFilesWithExerciseWordsMap() {
        dictionaryByLocale.forEach {
            val words = it.value
            words.remove("")
            val fileName = mapLocaleFile[it.key]
            val multilineText = StringBuilder()
            words.forEach { multilineText.append(it).append(System.lineSeparator()) }
            File(fileName).writeText(multilineText.toString())
        }
    }

    fun createTxtFilesWithDiagnosticWords(map: Map<String, String>) {
        val multilineText = StringBuilder()
        map.forEach { multilineText.append(it).append(System.lineSeparator()) }
        File(lopotkoFileName).writeText(multilineText.toString())
    }

    fun getExistWordFilesCount(locale: Locale) =
        File(localFolderForFiles + "/${locale.locale}").walkTopDown().filter { file -> file.isFile }.count()

    fun isFileExistLocal(audioFileMetaData: AudioFileMetaData): Boolean {
        val filePath = getLocalFilePathForWord(audioFileMetaData)
        return File(filePath).exists()
    }

    fun getFullS3UrlForWord(word: String, locale: String) =
        getFullS3UrlForWord(AudioFileMetaData(word, locale, getDefaultManVoiceForLocale(locale)))

    fun getFullS3UrlForWord(audioFileMetaData: AudioFileMetaData) =
        "$baseFileUrl${getSubFilePathForWord(audioFileMetaData)}"

    fun getLocalFilePathForWord(audioFileMetaData: AudioFileMetaData) =
        "$localFolderForFiles${getSubFilePathForWord(audioFileMetaData)}"

    fun getSubFilePathForWord(meta: AudioFileMetaData) =
        "${getSubPathForWord(meta)}/${DigestUtils.md5Hex(meta.text)}.ogg"

    fun getSubPathForWord(meta: AudioFileMetaData) =
        "/audio/${meta.locale}/${meta.voice.name.toLowerCase()}/${meta.speed}"

    fun getAudioFileUrlDynamically(index: Int, word: String): String =
        String.format(audioFileSeriesPath, index, word)
}

data class AudioFileMetaData(val text: String, val locale: String, val voice: Voice, val speed: String = "1") {
    override fun toString() = "(text = `$text`, locale=$locale, voice=${voice.name.toLowerCase()} speed=$speed)"
}
