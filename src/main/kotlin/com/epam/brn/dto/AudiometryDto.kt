package com.epam.brn.dto

import com.epam.brn.enums.AudiometryType
import javax.validation.constraints.NotBlank

data class AudiometryDto(
    val id: Long?,
    @NotBlank
    var locale: String?,
    @NotBlank
    var name: String,
    val audiometryType: AudiometryType,
    var description: String?,
    val audiometryTasks: Map<String, List<AudiometryTaskDto>>
)
