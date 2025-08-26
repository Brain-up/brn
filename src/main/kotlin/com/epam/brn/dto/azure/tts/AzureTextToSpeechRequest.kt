package com.epam.brn.dto.azure.tts

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

// https://docs.microsoft.com/en-us/azure/cognitive-services/speech-service/speech-synthesis-markup?tabs=csharp
// <speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xml:lang="string"></speak>
@JsonInclude(JsonInclude.Include.NON_NULL)
@JacksonXmlRootElement(localName = "speak")
data class AzureTextToSpeechRequest(
    @field:JacksonXmlProperty(isAttribute = true, localName = "version")
    var version: String = "1.0",
    @field:JacksonXmlProperty(isAttribute = true, localName = "xmlns")
    var xmlns: String = "http://www.w3.org/2001/10/synthesis",
    @field:JacksonXmlProperty(isAttribute = true, localName = "xmlns:mstts")
    var xmlnsMstts: String = "https://www.w3.org/2001/mstts",
    @field:JacksonXmlProperty(isAttribute = true, localName = "xml:lang")
    var lang: String,
    @field:JacksonXmlProperty(localName = "voice")
    var voice: Voice,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Voice(
    @field:JacksonXmlProperty(isAttribute = true, localName = "xml:lang")
    var lang: String,
    @field:JacksonXmlProperty(isAttribute = true, localName = "xml:gender")
    var gender: String,
    @field:JacksonXmlProperty(isAttribute = true)
    var name: String,
    @field:JacksonXmlText
    var text: String? = null,
    @field:JacksonXmlProperty
    var prosody: Prosody? = null,
)

// <mstts:express-as style="string" styledegree="value"></mstts:express-as>
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ExpressAs(
    @field:JacksonXmlProperty(isAttribute = true)
    var role: String? = null,
    @field:JacksonXmlProperty(isAttribute = true)
    var style: String? = null,
    @field:JacksonXmlProperty(isAttribute = true)
    var styledegree: String? = null,
    @field:JacksonXmlText
    var text: String? = null,
)

// <prosody pitch="value" contour="value" range="value" rate="value" volume="value"></prosody>
data class Prosody(
    @field:JacksonXmlProperty(isAttribute = true)
    var pitch: String? = null,
    @field:JacksonXmlProperty(isAttribute = true)
    var contour: String? = null,
    @field:JacksonXmlProperty(isAttribute = true)
    var range: String? = null,
    @field:JacksonXmlProperty(isAttribute = true)
    var rate: String? = null,
    @field:JacksonXmlProperty(isAttribute = true)
    var volume: String? = null,
    @field:JacksonXmlProperty(localName = "mstts:express-as")
    var expressAs: ExpressAs? = null,
    @field:JacksonXmlText
    var text: String? = null,
)
