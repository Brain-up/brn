package com.epam.brn.service

import org.springframework.stereotype.Service

@Service
class WordAnalyzingService {

    val vowels = "а,е,ё,и,о,у,э,ы,ю,я".toCharArray()

    fun findSyllableCount(word: String): Int {
        var syllableCount = 0
        word.toCharArray().forEach { if (vowels.contains(it)) syllableCount++ }
        return syllableCount
    }
}
