package com.epam.brn.service

import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.Voice
import com.epam.brn.dto.AudioFileMetaData
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

    private val mapYandexLocaleManVoice =
        mapOf(BrnLocale.RU.locale to Voice.FILIPP, BrnLocale.EN.locale to Voice.NICK, BrnLocale.TR.locale to Voice.ERKANYAVAS)

    private val mapYandexLocaleWomanVoice =
        mapOf(BrnLocale.RU.locale to Voice.OKSANA, BrnLocale.EN.locale to Voice.ALYSS, BrnLocale.TR.locale to Voice.SILAERKAN)

    fun getDefaultManVoiceForLocale(locale: String): String = mapYandexLocaleManVoice[locale]!!.name
    fun getDefaultWomanVoiceForLocale(locale: String): String = mapYandexLocaleWomanVoice[locale]!!.name

    fun getVoicesForLocale(locale: String): List<String?> =
        listOf(mapYandexLocaleManVoice[locale]?.name, mapYandexLocaleWomanVoice[locale]?.name)

    val dictionaryByLocale =
        mutableMapOf(BrnLocale.RU to mutableMapOf<String, String>(), BrnLocale.EN to mutableMapOf<String, String>())

    lateinit var mapLocaleFile: Map<BrnLocale, String>

    @PostConstruct
    private fun init() {
        mapLocaleFile = mapOf(BrnLocale.RU to wordsFileNameRu, BrnLocale.EN to wordsFileNameEn)
    }

    fun addWordsToDictionary(locale: BrnLocale, words: Collection<String>) =
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

    fun getExistWordFilesCount(locale: BrnLocale) =
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
        "/audio/${meta.locale}/${meta.voice.toLowerCase()}/${meta.speedFloat}"
}
