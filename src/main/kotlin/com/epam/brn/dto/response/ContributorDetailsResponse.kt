package com.epam.brn.dto.response

data class ContributorDetailsResponse(
    var id: Long?,
    var type: String,
    var name: String?,
    var description: String?,
    var company: String?,
    var nameEn: String?,
    var descriptionEn: String?,
    var companyEn: String?,
    var pictureUrl: String?,
    var contacts: Set<ContactResponse>,
    var gitHubUser: GitHubUserResponse?,
    var active: Boolean
)
