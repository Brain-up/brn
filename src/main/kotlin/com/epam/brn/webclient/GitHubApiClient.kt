package com.epam.brn.webclient

import com.epam.brn.webclient.model.GitHubContributor
import com.epam.brn.webclient.model.GitHubUser
import com.epam.brn.webclient.property.GitHubApiClientProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class GitHubApiClient @Autowired constructor(
    private val gitHubApiClientProperty: GitHubApiClientProperty,
    private val gitHubApiWebClient: WebClient
) {

    fun getContributors(organizationName: String, repositoryName: String, pageSize: Int): List<GitHubContributor> {
        val gitHubContributors = mutableListOf<GitHubContributor>()
        var page = 1
        while (true) {
            val portions = gitHubApiWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path(gitHubApiClientProperty.url.path.contributors)
                        .queryParam("per_page", pageSize)
                        .queryParam("page", page)
                        .build(organizationName, repositoryName)
                }
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", gitHubApiClientProperty.token)
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<List<GitHubContributor>>() {})
                .onErrorResume {
                    println("onErrorResume $it")
                    Mono.empty()
                }
                .block()
            if (portions.isNullOrEmpty()) {
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
            .onStatus(HttpStatus::isError) { Mono.empty() }
            .bodyToMono(GitHubUser::class.java)
            .onErrorResume { Mono.empty() }
            .block()
    }
}
