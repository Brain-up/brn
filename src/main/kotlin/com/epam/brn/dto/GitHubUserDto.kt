package com.epam.brn.dto

data class GitHubUserDto(
    var id: Long? = null,
    var name: String? = null,
    var email: String? = null,
    var avatarUrl: String?,
    var bio: String?,
    var company: String?,
)
