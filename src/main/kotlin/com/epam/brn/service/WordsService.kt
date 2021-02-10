package com.epam.brn.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

@Service
class WordsService {

    @Value(value = "\${yandex.folderForFiles}")
    private lateinit var folderForFiles: String

    val fullWordsSet = HashSet<String>()

    fun createTxtFileWithExerciseWordsMap(wordHashMap: MutableMap<String, String>, fileName: String): File {
        wordHashMap.remove("")
        fullWordsSet.addAll(wordHashMap.keys)
        val file = File(fileName)
        val multilineText = StringBuilder()
        wordHashMap.forEach { multilineText.append(it).append(System.lineSeparator()) }
        File(fileName).writeText(multilineText.toString())
        return file
    }

    fun getExistWordFiles(): HashSet<String> {
        val existsFileNames = HashSet<String>()
        File(folderForFiles).walkTopDown().forEach { f -> existsFileNames.add(f.nameWithoutExtension) }
        return existsFileNames
    }
}
