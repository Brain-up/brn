package com.epam.brn.service.yandex.tts.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("yandex")
@ConditionalOnProperty(name = ["default.tts.provider"], havingValue = "yandex")
class YandexTtsProperties {
    lateinit var authToken: String
    lateinit var getTokenLink: String
    lateinit var generationAudioLink: String
    lateinit var folderId: String
    var preferredRole: String? = null
    lateinit var folderForFiles: String
}
