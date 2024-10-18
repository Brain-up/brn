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
            BrnLocale.RU.locale to Voice.FILIPP,
            BrnLocale.EN.locale to Voice.NICK,
            BrnLocale.TR.locale to Voice.ERKANYAVAS
        )

    private val mapYandexLocaleWomanVoice =
        mapOf(
            BrnLocale.RU.locale to Voice.OKSANA,
            BrnLocale.EN.locale to Voice.ALYSS,
            BrnLocale.TR.locale to Voice.SILAERKAN
        )

    fun getDefaultManVoiceForLocale(locale: String): String =
        mapYandexLocaleManVoice[locale]!!.name
    fun getDefaultWomanVoiceForLocale(locale: String): String =
        mapYandexLocaleWomanVoice[locale]!!.name

    fun getVoicesForLocale(locale: String): List<String?> =
        listOf(mapYandexLocaleManVoice[locale]?.name, mapYandexLocaleWomanVoice[locale]?.name)

    fun getLocalFilePathForWord(audioFileMetaData: AudioFileMetaData) =
        "$localFolderForFiles${getSubFilePathForWord(audioFileMetaData)}"

    fun getSubFilePathForWord(meta: AudioFileMetaData) =
        "${getSubPathForWord(meta)}/${DigestUtils.md5Hex(meta.text)}.ogg"

    fun getSubPathForWord(meta: AudioFileMetaData) =
        "/audio/${meta.locale}/${meta.voice.lowercase()}/${meta.speedFloat}"
}
