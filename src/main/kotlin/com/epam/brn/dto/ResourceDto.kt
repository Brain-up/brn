package com.epam.brn.dto

data class ResourceDto(
    var id: Long? = null,
    val audioFileUrl: String? = "",
    val word: String? = "",
    val pictureFileUrl: String? = "",
    val soundsCount: Int? = 0
)