package com.epam.brn.dto

data class ContributorAdminDto(
    var id: Long?,
    var type: String,
    var name: String?,
    var description: String?,
    var company: String?,
    var nameEn: String?,
    var descriptionEn: String?,
    var companyEn: String?,
    var pictureUrl: String?,
    var contacts: Set<ContactDto>,
    var gitHubUser: GitHubUserDto?,
    var active: Boolean
)
