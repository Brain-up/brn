package com.epam.brn.webclient

import com.epam.brn.webclient.model.GitHubContributor
import com.epam.brn.webclient.model.GitHubUser
import com.epam.brn.webclient.property.GitHubApiClientProperty
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow

@Component
class GitHubApiClient(
    val gitHubApiClientProperty: GitHubApiClientProperty,
    val gitHubApiWebClient: WebClient
) {

    fun getContributors(organizationName: String, repositoryName: String, pageSize: Int): List<GitHubContributor> {
        val gitHubContributors = mutableListOf<GitHubContributor>()
        var page = 1
        while (true) {
            val portions = runBlocking {
                gitHubApiWebClient.get()
                    .uri { uriBuilder ->
                        uriBuilder.path(gitHubApiClientProperty.url.path.contributors)
                            .queryParam("per_page", pageSize)
                            .queryParam("page", page)
                            .build(organizationName, repositoryName)
                    }
                    .header("Accept", "application/vnd.github+json")
                    .header("Authorization", gitHubApiClientProperty.token)
                    .retrieve()
                    .bodyToFlow<GitHubContributor>()
                    .toList()
            }
            if (portions.isEmpty()) {
                break
            } else {
                gitHubContributors.addAll(portions)
                page++
                if (portions.size < pageSize) {
                    break
                }
            }
        }
        return gitHubContributors
    }

    fun getUser(username: String): GitHubUser? {
        return gitHubApiWebClient.get()
            .uri(gitHubApiClientProperty.url.path.users, username)
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", gitHubApiClientProperty.token)
            .retrieve()
            .bodyToMono(GitHubUser::class.java)
            .block()
    }
}
