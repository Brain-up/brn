package com.epam.brn.dto.response

data class GitHubUserResponse(
    var id: Long? = null,
    var name: String? = null,
    var email: String? = null,
    var avatarUrl: String?,
    var bio: String?,
    var company: String?,
)
