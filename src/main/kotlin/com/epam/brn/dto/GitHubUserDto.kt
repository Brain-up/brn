package com.epam.brn.dto

data class GitHubUserDto(
    var id: Long,
    var login: String?,
    var name: String?,
    var email: String?,
    var avatarUrl: String?,
    var bio: String?,
    var company: String?,
)
