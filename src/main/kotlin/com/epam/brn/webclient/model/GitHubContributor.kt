package com.epam.brn.webclient.model

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubContributor(
    var id: Long,
    var login: String,
    @JsonProperty("avatar_url")
    var avatarUrl: String?,
    @JsonProperty("gravatar_id")
    var gravatarId: String?,
    var url: String?,
    var type: String?,
    @JsonProperty("site_admin")
    var siteAdmin: Boolean?,
    val contributions: Long,
)
