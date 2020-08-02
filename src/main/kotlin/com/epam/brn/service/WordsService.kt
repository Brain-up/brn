package com.epam.brn.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

@Service
class WordsService {

    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var folderForFiles: String

    val fullWordsSet = HashSet<String>()
    val wordsWithoutAudioResourceSet = HashSet<String>()

    fun createTxtFileWithExerciseWords(words: MutableSet<String>, fileName: String): File {
        words.remove("")
        fullWordsSet.addAll(words)
        val file = File(fileName)
        File(fileName).writeText(words.joinToString(","))
        return file
    }

    fun fillWordsWithoutAudioOggFile() {
        wordsWithoutAudioResourceSet.addAll(fullWordsSet)
        val fileNames = HashSet<String>()
        File(folderForFiles).walkTopDown().forEach { f -> fileNames.add(f.nameWithoutExtension) }
        wordsWithoutAudioResourceSet.removeAll(fileNames)
    }
}
