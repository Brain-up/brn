package com.epam.brn.service.azure.tts

import com.epam.brn.dto.azure.tts.AzurePitches
import com.epam.brn.dto.azure.tts.AzureRates
import com.epam.brn.dto.azure.tts.AzureTextToSpeechRequest
import com.epam.brn.dto.azure.tts.ExpressAs
import com.epam.brn.dto.azure.tts.Prosody
import com.epam.brn.dto.azure.tts.Voice
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.json.JsonTest

@JsonTest
class TextToSpeechRequestTest {
    @Test
    @Throws(JsonProcessingException::class)
    fun whenSerializedToXmlStr_thenCorrect() {
        val xmlMapper =
            XmlMapper(JacksonXmlModule().apply { setDefaultUseWrapper(false) })
                .apply { enable(SerializationFeature.INDENT_OUTPUT) }
        val textToSpeechRequest =
            AzureTextToSpeechRequest(
                voice =
                    Voice(
                        name = "en-US-ChristopherNeural",
                        gender = "Male",
                        lang = "en-US",
                        prosody =
                            Prosody(
                                pitch = AzurePitches.DEFAULT.code,
                                rate = AzureRates.DEFAULT.code,
                                expressAs =
                                    ExpressAs(
                                        style = "newscast-casual",
                                        styledegree = "1",
                                        text = "Microsoft Speech Service Text-to-Speech API",
                                    ),
                            ),
                    ),
                lang = "en-US",
            )
        val xml = xmlMapper.writeValueAsString(textToSpeechRequest)
        assertNotNull(xml)
    }
}
