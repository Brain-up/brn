package com.epam.brn.webclient

import com.epam.brn.dto.github.GitHubContributorDto
import com.epam.brn.dto.github.GitHubUserDto
import com.epam.brn.webclient.config.GitHubApiClientConfig
import com.epam.brn.webclient.property.GitHubApiClientProperty
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.junit5.MockKExtension
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@ExtendWith(MockKExtension::class)
internal class GitHubApiClientTest {
    private lateinit var client: GitHubApiClient

    private lateinit var server: MockWebServer

    private lateinit var gitHubApiClientProperty: GitHubApiClientProperty

    private lateinit var gitHubApiClientConfig: GitHubApiClientConfig

    @BeforeEach
    fun setup() {
        server = MockWebServer()
        server.start()
        val baseRoot = "/"
        val rootUrl = server.url(baseRoot).toString()
        gitHubApiClientProperty =
            GitHubApiClientProperty(
                "token",
                "type",
                GitHubApiClientProperty.GitHubApiUrl(
                    rootUrl,
                    GitHubApiClientProperty.GitHubApiUrl.GitHubApiPath(
                        "/contributors",
                        "/users",
                    ),
                ),
                16777216,
                false,
                15000,
                30000,
            )
        gitHubApiClientConfig = GitHubApiClientConfig(gitHubApiClientProperty)
        client = GitHubApiClient(gitHubApiClientProperty, gitHubApiClientConfig.gitHubApiWebClient(WebClient.builder()))
    }

    @AfterEach
    fun afterAll() {
        server.shutdown()
    }

    @Test
    fun getContributorsWhenOKShouldReturnContributors() {
        val contributor =
            GitHubContributorDto(
                login = "lifeart",
                id = 1360552,
                gravatarId = "",
                avatarUrl = "https://avatars.githubusercontent.com/u/1360552?v=4",
                url = "https://api.github.com/users/lifeart",
                type = "User",
                siteAdmin = false,
                contributions = 312,
            )

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(readResourceAsString("contributors.json")),
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(readResourceAsString("contributors-2.json")),
        )

        val contributors = client.getGitHubContributors("Brain-Up", "brn", 50)

        assertSoftly {
            contributors.shouldNotBeEmpty()
            contributors.size shouldBe 56
            contributors[0].shouldBeInstanceOf<GitHubContributorDto>()
            contributors[0] shouldBe contributor
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [401, 403, 404, 500])
    fun getContributorsWhenHttpErrorThenShouldReturnEmptyContributors(statusCode: Int) {
        server.enqueue(
            MockResponse()
                .setResponseCode(statusCode)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(readResourceAsString("http-error.json")),
        )

        val contributors = client.getGitHubContributors("Brain-Up", "brn", 50)

        contributors.shouldBeEmpty()
    }

    @Test
    fun getUserWhenOKShouldReturnGotHubUserInfo() {
        val expectedUser =
            GitHubUserDto(
                id = 7206824,
                login = "test-user",
                avatarUrl = "https://avatars.githubusercontent.com/u/test-user?v=4",
                gravatarId = "",
                name = "Test User",
                company = "Company",
                location = "Location",
                email = "email@email.com",
                bio = "Fullstack Developer",
            )
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(readResourceAsString("user.json")),
        )

        val user = client.getGitHubUser("test-user")

        assertSoftly {
            user.shouldNotBeNull()
            user.shouldBeInstanceOf<GitHubUserDto>()
            user shouldBe expectedUser
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [404, 500])
    fun getUserWhenHttpErrorThenShouldReturnNull(statusCode: Int) {
        server.enqueue(
            MockResponse()
                .setResponseCode(statusCode)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(readResourceAsString("http-error.json")),
        )

        val user = client.getGitHubUser("test-user")

        user.shouldBeNull()
    }

    private fun readResourceAsString(fileName: String) = this::class.java.getResource("/inputData/githubapi/$fileName").readText()
}
