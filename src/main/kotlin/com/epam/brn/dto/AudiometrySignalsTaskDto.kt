package com.epam.brn.dto

import com.epam.brn.enums.EAR

data class AudiometrySignalsTaskDto(
    val id: Long?,
    val ear: EAR,
    val frequencies: List<Int>,
)
