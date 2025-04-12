package com.epam.brn.webclient

import com.epam.brn.dto.github.GitHubContributorDto
import com.epam.brn.dto.github.GitHubUserDto
import com.epam.brn.webclient.property.GitHubApiClientProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class GitHubApiClient
    @Autowired
    constructor(
        private val gitHubApiClientProperty: GitHubApiClientProperty,
        private val gitHubApiWebClient: WebClient,
    ) {
        fun getGitHubContributors(
            organizationName: String,
            repositoryName: String,
            pageSize: Int,
        ): List<GitHubContributorDto> {
            val gitHubContributorDtos = mutableListOf<GitHubContributorDto>()
            var page = 1
            while (true) {
                val portions =
                    gitHubApiWebClient
                        .get()
                        .uri { uriBuilder ->
                            uriBuilder
                                .path(gitHubApiClientProperty.url.path.contributors)
                                .queryParam("per_page", pageSize)
                                .queryParam("page", page)
                                .build(organizationName, repositoryName)
                        }.headers {
                            it.set(HttpHeaders.ACCEPT, "application/vnd.github+json")
                            if (gitHubApiClientProperty.token.isNotEmpty())
                                it.set(
                                    HttpHeaders.AUTHORIZATION,
                                    "${gitHubApiClientProperty.typeToken} ${gitHubApiClientProperty.token}",
                                )
                        }.retrieve()
                        .bodyToMono(object : ParameterizedTypeReference<List<GitHubContributorDto>>() {})
                        .onErrorResume {
                            println("onErrorResume $it")
                            Mono.empty()
                        }.block()
                if (portions.isNullOrEmpty()) {
                    break
                } else {
                    gitHubContributorDtos.addAll(portions)
                    page++
                    if (portions.size < pageSize) {
                        break
                    }
                }
            }
            return gitHubContributorDtos
        }

        fun getGitHubUser(username: String): GitHubUserDto? = gitHubApiWebClient
            .get()
            .uri(gitHubApiClientProperty.url.path.users, username)
            .headers {
                it.set(HttpHeaders.ACCEPT, "application/vnd.github+json")
                if (gitHubApiClientProperty.token.isNotEmpty())
                    it.set(
                        HttpHeaders.AUTHORIZATION,
                        "${gitHubApiClientProperty.typeToken} ${gitHubApiClientProperty.token}",
                    )
            }.retrieve()
            .onStatus(HttpStatus::isError) { Mono.empty() }
            .bodyToMono(GitHubUserDto::class.java)
            .onErrorResume { Mono.empty() }
            .block()
    }
