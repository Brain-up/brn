package com.epam.brn.dto.github

import com.epam.brn.model.GitHubUser
import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubUserDto(
    val id: Long,
    val login: String,
    @JsonProperty("avatar_url")
    val avatarUrl: String? = null,
    @JsonProperty("gravatar_id")
    val gravatarId: String? = null,
    val name: String? = null,
    val company: String? = null,
    val location: String? = null,
    val email: String? = null,
    val bio: String? = null,
) {
    fun toEntity(contributionsLevel: Long) =
        GitHubUser(
            id = id,
            name = name,
            login = login,
            email = email,
            avatarUrl = avatarUrl,
            bio = bio,
            company = company,
            contributions = contributionsLevel,
        )
}
