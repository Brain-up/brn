package com.epam.brn.webclient.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "github.api")
data class GitHubApiClientProperty(val username: String, val token: String, val url: GitHubApiUrl) {
    data class GitHubApiUrl(val base: String, val path: GitHubApiPath) {
        data class GitHubApiPath(val contributors: String, val users: String)
    }
}
