package com.epam.brn.dto.response

import com.epam.brn.dto.ContactDto
import com.epam.brn.dto.GitHubUserDto

data class ContributorDetailsResponse(
    var id: Long,
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
    var active: Boolean,
)
