package com.epam.brn.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

@Service
class WordsService {

    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var folderForFiles: String

    val fullWordsSet = HashSet<String>()

    fun createTxtFileWithExerciseWordsMap(words: MutableMap<String, String>, fileName: String): File {
        words.remove("")
        fullWordsSet.addAll(words.keys)
        val file = File(fileName)
        File(fileName).writeText(words.toString())
        return file
    }

    fun getExistWordFiles(): HashSet<String> {
        val existsFileNames = HashSet<String>()
        File(folderForFiles).walkTopDown().forEach { f -> existsFileNames.add(f.nameWithoutExtension) }
        return existsFileNames
    }
}
