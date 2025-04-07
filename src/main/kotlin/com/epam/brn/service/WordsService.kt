package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.Voice
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class WordsService {
    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var localFolderForFiles: String

    private val mapYandexLocaleManVoice =
        mapOf(
            BrnLocale.RU.locale to listOf(Voice.FILIPP, Voice.ALEXANDER, Voice.KIRILL),
            BrnLocale.EN.locale to listOf(Voice.JOHN, Voice.NICK),
            BrnLocale.TR.locale to listOf(Voice.ERKANYAVAS),
        )

    private val mapYandexLocaleWomanVoice =
        mapOf(
            BrnLocale.RU.locale to listOf(Voice.MARINA, Voice.DASHA, Voice.OKSANA, Voice.LERA),
            BrnLocale.EN.locale to listOf(Voice.JOHN, Voice.ALYSS),
            BrnLocale.TR.locale to listOf(Voice.SILAERKAN),
        )

    fun getDefaultManVoiceForLocale(locale: String): String = mapYandexLocaleManVoice[locale]!!.first().name

    fun getDefaultWomanVoiceForLocale(locale: String): String = mapYandexLocaleWomanVoice[locale]!!.first().name

    fun getVoicesForLocale(locale: String): List<String> =
        mapYandexLocaleManVoice[locale]!!.map { it.name }.plus(mapYandexLocaleWomanVoice[locale]!!.map { it.name })

    fun getLocalFilePathForWord(audioFileMetaData: AudioFileMetaData) = "$localFolderForFiles${getSubFilePathForWord(audioFileMetaData)}"

    fun getSubFilePathForWord(meta: AudioFileMetaData) = "${getSubPathForWord(meta)}/${DigestUtils.md5Hex(meta.text)}.ogg"

    fun getSubPathForWord(meta: AudioFileMetaData) = "/audio/${meta.locale}/${meta.voice.lowercase()}/${meta.speedFloat}"
}
