package com.epam.brn.webclient.model

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubUser(
    val id: Long,
    val login: String,
    @JsonProperty("avatar_url")
    val avatarUrl: String?,
    @JsonProperty("gravatar_id")
    val gravatarId: String?,
    val name: String?,
    val company: String?,
    val location: String?,
    val email: String?,
    val bio: String?,
)
