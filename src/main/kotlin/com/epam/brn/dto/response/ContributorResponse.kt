package com.epam.brn.dto.response

import com.epam.brn.dto.ContactDto
import com.epam.brn.enums.ContributorType

data class ContributorResponse(
    var id: Long,
    var name: String? = null,
    var description: String? = null,
    var company: String? = null,
    var pictureUrl: String? = null,
    var contacts: Set<ContactDto>? = null,
    var type: ContributorType? = null,
    var contribution: Long
)
