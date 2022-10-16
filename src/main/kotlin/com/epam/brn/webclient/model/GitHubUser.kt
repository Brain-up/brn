package com.epam.brn.webclient.model

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubUser(
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
)
