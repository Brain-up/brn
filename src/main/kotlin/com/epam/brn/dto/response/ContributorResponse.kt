package com.epam.brn.dto.response

import com.epam.brn.dto.ContactDto

data class ContributorResponse(
    var id: Long,
    var name: String?,
    var description: String?,
    var company: String?,
    var pictureUrl: String?,
    var contacts: Set<ContactDto>,
    var contribution: Long
)
