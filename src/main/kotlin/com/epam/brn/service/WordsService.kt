package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.Voice
import com.epam.brn.enums.VoiceGender
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class WordsService {
    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var localFolderForFiles: String

    private val runtimeDefaultVoices = ConcurrentHashMap<String, Voice>()

    fun getDefaultManVoiceForLocale(locale: String): String = getVoiceEnumsForLocale(locale, VoiceGender.MALE).first().name

    fun getDefaultWomanVoiceForLocale(locale: String): String = getVoiceEnumsForLocale(locale, VoiceGender.FEMALE)
        .firstOrNull()
        ?.name
        ?: getDefaultManVoiceForLocale(locale)

    fun getDefaultVoiceForLocale(locale: String): String = getDefaultVoiceEnumForLocale(locale).name

    fun getVoicesForLocale(locale: String): List<String> = getVoiceEnumsForLocale(locale).map { it.name }

    fun getAvailableVoicesForLocale(locale: String): List<Voice> = getVoiceEnumsForLocale(locale)

    fun getVoiceForLocale(
        locale: String,
        voice: String,
    ): Voice? = Voice.findByValue(voice)?.takeIf { it.locale == locale.lowercase() }

    fun setDefaultVoiceForLocale(
        locale: String,
        voice: String,
    ): Voice {
        val localeVoices = getVoiceEnumsForLocale(locale)
        if (localeVoices.isEmpty())
            throw IllegalArgumentException("Locale $locale does not support yet for generation audio files.")

        val selectedVoice =
            getVoiceForLocale(locale, voice)
                ?: throw IllegalArgumentException(
                    "Locale $locale does not support voice $voice, only ${localeVoices.map { it.name }}.",
                )

        runtimeDefaultVoices[locale.lowercase()] = selectedVoice
        return selectedVoice
    }

    fun getLocalFilePathForWord(audioFileMetaData: AudioFileMetaData) = "$localFolderForFiles${getSubFilePathForWord(audioFileMetaData)}"

    fun getSubFilePathForWord(meta: AudioFileMetaData) = "${getSubPathForWord(meta)}/${DigestUtils.md5Hex(meta.text)}.ogg"

    fun getSubPathForWord(meta: AudioFileMetaData) = "/audio/${meta.locale}/${meta.voice.lowercase()}/${meta.speedFloat}"

    private fun getVoiceEnumsForLocale(
        locale: String,
        gender: VoiceGender? = null,
    ): List<Voice> = Voice
        .getVoicesForLocale(locale)
        .filter { gender == null || it.gender == gender }

    private fun getDefaultVoiceEnumForLocale(locale: String): Voice {
        val normalizedLocale = locale.lowercase()

        return runtimeDefaultVoices[normalizedLocale]
            ?: getVoiceEnumsForLocale(normalizedLocale, VoiceGender.MALE).firstOrNull()
            ?: getVoiceEnumsForLocale(normalizedLocale).firstOrNull()
            ?: throw IllegalArgumentException("Locale $locale does not support yet for generation audio files.")
    }
}
