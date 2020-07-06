package com.epam.brn.service

import org.springframework.stereotype.Service
import java.util.Locale
import javax.speech.Central
import javax.speech.synthesis.Synthesizer
import javax.speech.synthesis.SynthesizerModeDesc
import javax.speech.AudioException
import javax.speech.EngineException
import javax.speech.EngineStateError
import java.beans.PropertyVetoException
import javax.speech.synthesis.Voice

@Service
class GenerationAudioFilesService {
    var desc: SynthesizerModeDesc? = null
    lateinit var synthesizer: Synthesizer

    @Throws(EngineException::class, AudioException::class, EngineStateError::class, PropertyVetoException::class)
    fun init(voiceName: String) {
        if (desc == null) {
            System.setProperty(
                "freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory"
            )
            desc = SynthesizerModeDesc(Locale.US)
            Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral")
            synthesizer = Central.createSynthesizer(desc)
            synthesizer.allocate()
            synthesizer.resume()
            val smd = synthesizer.engineModeDesc as SynthesizerModeDesc
            val voices = smd.voices
            var voice: Voice? = null
            for (i in voices.indices) {
                if (voices[i].name.equals(voiceName)) {
                    voice = voices[i]
                    break
                }
            }
            synthesizer.synthesizerProperties.voice = voice
        }
    }

    @Throws(EngineException::class, EngineStateError::class)
    fun terminate() {
        synthesizer.deallocate()
    }

    @Throws(EngineException::class, AudioException::class, IllegalArgumentException::class, InterruptedException::class)
    fun doSpeak(speakText: String) {
        synthesizer.speakPlainText(speakText, null)
        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY)
    }

    fun main(args: Array<String>) {
        val generationAudioFilesService = GenerationAudioFilesService()

        generationAudioFilesService.init("kevin16")
        // high quality
        generationAudioFilesService.doSpeak("Hello world from Real's How To")
        generationAudioFilesService.terminate()
    }
}
