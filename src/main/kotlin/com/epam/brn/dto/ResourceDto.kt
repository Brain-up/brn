package com.epam.brn.dto

import com.epam.brn.model.WordType

data class ResourceDto(
    var id: Long? = null,
    var audioFileUrl: String? = "",
    val word: String? = "",
    val wordType: WordType?,
    val pictureFileUrl: String? = "",
    var soundsCount: Int? = 0,
    val description: String? = "",
    var columnNumber: Int = -1, // -1 - if never-mind, or column number from 0, 1, ..
)
