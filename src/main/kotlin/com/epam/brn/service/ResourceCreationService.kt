package com.epam.brn.service

import java.io.File
import org.springframework.stereotype.Service

@Service
class ResourceCreationService {

    fun createFileWithWords(words: Set<String>, fileName: String): File {
        val file = File(fileName)
        File(fileName).writeText(words.joinToString(","))
        return file
    }
}
