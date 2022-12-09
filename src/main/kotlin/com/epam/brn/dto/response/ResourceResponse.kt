package com.epam.brn.dto.response

import com.epam.brn.enums.WordType

data class ResourceResponse(
    var id: Long? = null,
    val word: String? = "",
    val wordPronounce: String? = "",
    val wordType: WordType?,
    @Deprecated("Will be removed since it's not needed anymore")
    val pictureFileUrl: String? = "",
    var soundsCount: Int? = 0,
    val description: String? = "",
    var columnNumber: Int = -1, // -1 - if never-mind, or column number from 0, 1, ..
)
