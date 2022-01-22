package com.epam.brn.dto.response

import com.epam.brn.enums.EAR

data class AudiometrySignalsTaskResponse(
    val id: Long?,
    val ear: EAR,
    val frequencies: List<Int>,
)
