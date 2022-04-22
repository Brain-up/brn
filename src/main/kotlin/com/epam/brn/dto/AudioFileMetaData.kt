package com.epam.brn.dto

data class AudioFileMetaData(
    val text: String,
    val locale: String,
    val voice: String,
    val speed: String = "1",
    val gender: String? = null,
    val pitch: String? = null,
    val style: String? = null,
    val styleDegree: String = "1"
)
