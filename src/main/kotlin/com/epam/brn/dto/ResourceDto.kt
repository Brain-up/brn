package com.epam.brn.dto

import com.epam.brn.constant.WordTypeEnum

data class ResourceDto(
    var id: Long? = null,
    val audioFileUrl: String? = "",
    val word: String? = "",
    val wordType: WordTypeEnum?,
    val pictureFileUrl: String? = "",
    val soundsCount: Int? = 0
)