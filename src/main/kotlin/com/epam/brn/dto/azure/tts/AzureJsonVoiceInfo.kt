package com.epam.brn.dto.azure.tts

import com.epam.brn.model.azure.tts.AzureSpeechStyle
import com.epam.brn.model.azure.tts.AzureVoiceInfo
import com.fasterxml.jackson.annotation.JsonProperty

class AzureJsonVoiceInfo(
    @field:JsonProperty("Name")
    var name: String,
    @field:JsonProperty("DisplayName")
    var displayName: String,
    @field:JsonProperty("LocalName")
    var localName: String,
    @field:JsonProperty("ShortName")
    var shortName: String,
    @field:JsonProperty("Gender")
    var gender: String,
    @field:JsonProperty("Locale")
    var locale: String,
    @field:JsonProperty("LocaleName")
    var localeName: String,
    @field:JsonProperty("StyleList")
    var styleList: MutableList<String>?,
    @field:JsonProperty("SampleRateHertz")
    var sampleRateHertz: String,
    @field:JsonProperty("VoiceType")
    var voiceType: String,
    @field:JsonProperty("Status")
    var status: String
) {
    fun convertToEntity(styles: MutableSet<AzureSpeechStyle> = mutableSetOf()) = AzureVoiceInfo(
        name = name,
        displayName = displayName,
        localName = localName,
        shortName = shortName,
        gender = gender,
        locale = locale,
        localeName = localeName,
        sampleRateHertz = sampleRateHertz,
        voiceType = voiceType,
        status = status,
        styleList = styles.toMutableList()
    )
}
