package com.epam.brn.dto.request.contributor

import com.epam.brn.enums.ContactType
import com.epam.brn.model.Contact
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class ContactRequest(
    @field:NotNull
    val type: ContactType?,
    @field:NotBlank
    @field:Length(max = 255)
    val value: String?,
) {
    fun toEntity() =
        Contact(
            type = this.type!!,
            value = this.value!!,
        )
}
