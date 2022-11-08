package com.epam.brn.dto.response

import com.epam.brn.dto.ContactDto
import com.epam.brn.enums.ContributorType

data class ContributorResponse(
    val id: Long,
    val gitHubLogin: String = "",
    val name: String,
    val nameEn: String?,
    val description: String? = null,
    val descriptionEn: String? = null,
    val company: String? = null,
    val companyEn: String? = null,
    val pictureUrl: String? = null,
    val contacts: Set<ContactDto> = emptySet(),
    val type: ContributorType,
    val contribution: Long = 0,
    val active: Boolean = true,
)
