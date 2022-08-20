package com.epam.brn.dto.response

data class ContributorResponse(
    var id: Long?,
    var name: String?,
    var description: String?,
    var company: String?,
    var pictureUrl: String?,
    var contacts: Set<ContactResponse>,
)
