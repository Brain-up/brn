package com.epam.brn.dto.response

import com.epam.brn.model.Gender
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.ZoneId
import java.time.ZonedDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserAccountDto(
    val id: Long? = null,
    val name: String,
    val email: String,
    val bornYear: Int,
    val gender: Gender,
    var active: Boolean,
    val created: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    val changed: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC")),
    val avatar: String? = null
) {
    var authorities: MutableSet<String>? = mutableSetOf()
}
