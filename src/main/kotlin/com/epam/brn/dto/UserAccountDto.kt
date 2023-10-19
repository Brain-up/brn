package com.epam.brn.dto

import com.epam.brn.enums.BrnGender
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import java.time.ZoneOffset

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserAccountDto(
    val id: Long? = null,
    val userId: String? = null,
    val name: String?,
    val email: String?,
    val bornYear: Int?,
    val gender: BrnGender?,
    var active: Boolean = true,
    val created: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val changed: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val lastVisit: LocalDateTime? = null,
    var avatar: String? = null,
    val photo: String? = null,
    val description: String? = null,
    var headphones: Set<HeadphonesDto>? = null,
    var doctorId: Long? = null
) {
    var roles: MutableSet<String> = mutableSetOf()
}
