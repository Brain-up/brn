package com.epam.brn.dto

import com.epam.brn.model.Resource

data class AudiometryLopotkoTaskResponse(
    val id: Long?,
    val level: Int,
    val audiometryGroup: String, // А, Б, В, Г
    val frequencyZone: String,
    val minFrequency: Int,
    val maxFrequency: Int,

    val count: Int = 10,
    val showSize: Int = 9,
    val answerOptions: MutableSet<Resource>
)
