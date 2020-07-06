package com.epam.brn.service

import org.springframework.stereotype.Service
import java.io.File

@Service
class ResourceCreationService {

    fun createFileWithWords(words: Set<String>, fileName: String): File {
        val file = File(fileName)
        File(fileName).writeText(words.joinToString(","))
        return file
    }
}
