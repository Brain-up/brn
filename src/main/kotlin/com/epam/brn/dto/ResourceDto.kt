package com.epam.brn.dto

import com.epam.brn.model.WordType

data class ResourceDto(
    var id: Long? = null,
    val audioFileUrl: String? = "",
    val word: String? = "",
    val wordType: WordType?,
    val pictureFileUrl: String? = "",
    val soundsCount: Int? = 0
)
