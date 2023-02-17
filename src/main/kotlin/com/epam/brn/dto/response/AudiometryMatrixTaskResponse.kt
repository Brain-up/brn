package com.epam.brn.dto.response

import com.epam.brn.model.Resource

data class AudiometryMatrixTaskResponse(
    val id: Long?,
    val count: Int = 10,
    val answerOptions: List<Resource>
)
