package com.epam.brn.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class WordAnalyzingService {

    @Value("#{'\${vowels}'.split(',')}")
    lateinit var vowels: List<Char>

    fun findSyllableCount(word: String): Int {
        var syllableCount = 0
        word.toCharArray().forEach { if (vowels.contains(it)) syllableCount++ }
        return syllableCount
    }
}
