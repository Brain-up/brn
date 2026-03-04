package com.epam.brn.enums

// Based on the current Yandex SpeechKit TTS voices docs.
enum class Voice(
    val locale: String,
    val gender: VoiceGender,
    vararg supportedRoles: VoiceRole,
) {
    FILIPP(BrnLocale.RU.locale, VoiceGender.MALE, VoiceRole.NEUTRAL),
    ERMIL(BrnLocale.RU.locale, VoiceGender.MALE, VoiceRole.NEUTRAL, VoiceRole.GOOD),
    ZAHAR(BrnLocale.RU.locale, VoiceGender.MALE),
    ALEXANDER(BrnLocale.RU.locale, VoiceGender.MALE, VoiceRole.NEUTRAL, VoiceRole.GOOD),
    KIRILL(BrnLocale.RU.locale, VoiceGender.MALE, VoiceRole.NEUTRAL, VoiceRole.STRICT, VoiceRole.GOOD),

    ALENA(BrnLocale.RU.locale, VoiceGender.FEMALE),
    OKSANA(BrnLocale.RU.locale, VoiceGender.FEMALE),
    MARINA(BrnLocale.RU.locale, VoiceGender.FEMALE, VoiceRole.FRIENDLY),
    DASHA(BrnLocale.RU.locale, VoiceGender.FEMALE),
    LERA(BrnLocale.RU.locale, VoiceGender.FEMALE),
    JULIA(BrnLocale.RU.locale, VoiceGender.FEMALE),
    MASHA(BrnLocale.RU.locale, VoiceGender.FEMALE),
    MADI_RU(BrnLocale.RU.locale, VoiceGender.FEMALE),
    OMAZH(BrnLocale.RU.locale, VoiceGender.FEMALE),

    JOHN(BrnLocale.EN.locale, VoiceGender.MALE),
    NICK(BrnLocale.EN.locale, VoiceGender.MALE),
    JANE(BrnLocale.EN.locale, VoiceGender.FEMALE),
    ALYSS(BrnLocale.EN.locale, VoiceGender.FEMALE),
    ;

    val supportedRoles: List<VoiceRole> = supportedRoles.toList()

    val apiValue: String
        get() = name.lowercase()

    companion object {
        fun getVoicesForLocale(locale: String): List<Voice> = values().filter { it.locale == locale.lowercase() }

        fun findByValue(value: String): Voice? = values().firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

enum class VoiceGender {
    MALE,
    FEMALE,
}

enum class VoiceRole {
    NEUTRAL,
    GOOD,
    FRIENDLY,
    STRICT,
    ;

    val apiValue: String
        get() = name.lowercase()

    companion object {
        fun findByValue(value: String): VoiceRole? = values().firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
