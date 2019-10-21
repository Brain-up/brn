package com.epam.brn.dto

data class ResourceDto(
    val id: Long? = null,
    val audioFileUrl: String,
    val word: String,
    val pictureFileUrl: String,
    val soundsCount: Int
)