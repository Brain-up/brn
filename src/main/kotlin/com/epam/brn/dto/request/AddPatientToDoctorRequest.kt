package com.epam.brn.dto.request

import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank

data class AddPatientToDoctorRequest(
    @field:Min(value = 1)
    var id: Long,
    @field:NotBlank
    var type: String
)
