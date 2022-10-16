package com.epam.brn.webclient.model

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubContributor(
    var id: Long,
    var login: String,
    @JsonProperty("avatar_url")
    var avatarUrl: String? = null,
    @JsonProperty("gravatar_id")
    var gravatarId: String? = null,
    var url: String? = null,
    var type: String? = null,
    @JsonProperty("site_admin")
    var siteAdmin: Boolean? = null,
    val contributions: Long
)
