package com.epam.brn.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class AddPatientToDoctorRequest(
    @field:Min(value = 1)
    var id: Long,
    @field:NotBlank
    var type: String,
)
