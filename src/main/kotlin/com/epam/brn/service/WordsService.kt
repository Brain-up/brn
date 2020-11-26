package com.epam.brn.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

@Service
class WordsService {

    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var folderForFiles: String

    val fullWordsSet = HashSet<String>()
    val existsFileNames = HashSet<String>()

    fun createTxtFileWithExerciseWords(words: MutableSet<String>, fileName: String): File {
        words.remove("")
        fullWordsSet.addAll(words)
        val file = File(fileName)
        File(fileName).writeText(words.joinToString(","))
        return file
    }

    fun createTxtFileWithExerciseWordsMap(words: MutableMap<String, String>, fileName: String): File {
        words.remove("")
        fullWordsSet.addAll(words.keys)
        val file = File(fileName)
        File(fileName).writeText(words.toString())
        return file
    }

    fun fillWordsWithAudioOggFile() {
        File(folderForFiles).walkTopDown().forEach { f -> existsFileNames.add(f.nameWithoutExtension) }
    }
}
