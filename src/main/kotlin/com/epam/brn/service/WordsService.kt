package com.epam.brn.service

import org.springframework.stereotype.Service
import java.io.File

@Service
class WordsService {

    val wordsSet = HashSet<String>()

    fun createFileWithWords(words: Set<String>, fileName: String): File {
        wordsSet.addAll(words)
        val file = File(fileName)
        File(fileName).writeText(words.joinToString(","))
        return file
    }
}
