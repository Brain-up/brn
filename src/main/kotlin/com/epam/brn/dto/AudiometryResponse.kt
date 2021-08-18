package com.epam.brn.dto

import com.epam.brn.enums.AudiometryType
import javax.validation.constraints.NotBlank

data class AudiometryResponse(
    val id: Long?,
    @field:NotBlank
    var locale: String,
    @field:NotBlank
    var name: String,
    val audiometryType: AudiometryType,
    var description: String?,
    val audiometryTasks: Any
)
