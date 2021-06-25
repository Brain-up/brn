package com.epam.brn.dto.response

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.model.Gender
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.ZoneId
import java.time.ZonedDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DoctorUserAccountDto(
    val id: Long? = null,
    val userId: String? = null,
    val name: String?,
    val email: String?,
    val bornYear: Int?,
    val gender: Gender?,
    var active: Boolean = true,
    val created: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    val changed: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    var avatar: String? = null,
    val photo: String? = null,
    val description: String? = null,
    var headphones: Set<HeadphonesDto>? = null,
    var patients: MutableList<UserAccountDto> = mutableListOf(),
) {
    var authorities: MutableSet<String>? = mutableSetOf()
}
