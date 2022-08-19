package com.epam.brn.dto

data class ContributorUserDto(
    var id: Long?,
    var name: String?,
    var description: String?,
    var company: String?,
    var pictureUrl: String?,
    var contacts: Set<ContactDto>,
)
