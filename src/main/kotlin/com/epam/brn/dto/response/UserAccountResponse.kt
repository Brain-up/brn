package com.epam.brn.dto.response

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.model.Gender
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import java.time.ZoneOffset

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserAccountResponse(
    val id: Long? = null,
    val userId: String? = null,
    val name: String?,
    val email: String?,
    val bornYear: Int?,
    val gender: Gender?,
    var active: Boolean = true,
    val created: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val changed: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    var avatar: String? = null,
    val photo: String? = null,
    val description: String? = null,
    var headphones: Set<HeadphonesDto>? = null,
    var doctorId: Long? = null
) {
    var authorities: MutableSet<String>? = mutableSetOf()
}
