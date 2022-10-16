package com.epam.brn.service.scheduler

import com.epam.brn.model.Contributor
import com.epam.brn.repo.ContributorRepository
import com.epam.brn.repo.GitHubUserRepository
import com.epam.brn.webclient.GitHubApiClient
import com.epam.brn.webclient.model.GitHubContributor
import com.epam.brn.webclient.model.GitHubUser
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class GitHubContributorRefreshServiceTest {

    @InjectMockKs
    private lateinit var service: GitHubContributorRefreshService

    @MockK
    private lateinit var gitHubApiClient: GitHubApiClient

    @MockK
    private lateinit var gitHubUserRepository: GitHubUserRepository

    @MockK
    private lateinit var contributorRepository: ContributorRepository

    @Test
    fun `should call needed method when new user income from github`() {
        // GIVEN
        val organizationName = "organization"
        val repositoryName = "repository"
        val pageSize = 10
        ReflectionTestUtils.setField(service, "gitHubOrganizationName", organizationName)
        ReflectionTestUtils.setField(service, "gitHubRepositoryName", repositoryName)
        ReflectionTestUtils.setField(service, "botLogins", setOf("bot"))
        ReflectionTestUtils.setField(service, "pageSize", pageSize)
        val gitHubContributors = listOf(
            GitHubContributor(
                id = 1,
                login = "login",
                contributions = 10
            ),
            GitHubContributor(
                id = 2,
                login = "login2",
                contributions = 20
            ),
        )
        every {
            gitHubApiClient.getContributors(organizationName, repositoryName, pageSize)
        } returns gitHubContributors

        val gitHubUsers = mutableListOf<GitHubUser>()
        val contributors = mutableListOf<Contributor>()
        for ((i, gitHubContributor) in gitHubContributors.withIndex()) {
            gitHubUsers.add(
                GitHubUser(
                    id = gitHubContributor.id,
                    login = gitHubContributor.login,
                )
            )
            contributors.add(
                Contributor(
                    id = i.toLong(),
                    name = gitHubContributor.login,
                    contribution = gitHubContributor.contributions
                )
            )
        }
        val savedGithubUser = mutableListOf<com.epam.brn.model.GitHubUser>()
        for ((i, gitHubUser) in gitHubUsers.withIndex()) {
            savedGithubUser.add(
                com.epam.brn.model.GitHubUser(
                    id = gitHubUser.id,
                    login = gitHubUser.login,
                    contributions = gitHubContributors[i].contributions,
                    name = null,
                    email = null,
                    avatarUrl = null,
                    bio = null,
                    company = null
                )
            )
        }

        every { gitHubApiClient.getUser(any()) } returns gitHubUsers[0]
        every { gitHubUserRepository.findById(any()) } returns Optional.empty()
        every { gitHubUserRepository.save(any()) } returnsMany savedGithubUser
        every { contributorRepository.findByGitHubUser(any()) } returns Optional.empty()
        every { contributorRepository.save(any()) } returnsMany contributors

        // WHEN
        service.synchronizeContributors()

        // THEN
        verify(exactly = 1) {
            gitHubApiClient.getContributors(any(), any(), any())
        }
        verify(exactly = gitHubContributors.size) {
            gitHubApiClient.getUser(any())
        }
        verify(exactly = gitHubContributors.size) {
            gitHubUserRepository.findById(any())
        }
        verify(exactly = gitHubContributors.size) {
            contributorRepository.findByGitHubUser(any())
        }
        verify(exactly = gitHubContributors.size) {
            contributorRepository.save(any())
        }
    }

    @Test
    fun `should call needed method when need update user (only contributors field) from github`() {
        // GIVEN
        val organizationName = "organization"
        val repositoryName = "repository"
        val pageSize = 10
        ReflectionTestUtils.setField(service, "gitHubOrganizationName", organizationName)
        ReflectionTestUtils.setField(service, "gitHubRepositoryName", repositoryName)
        ReflectionTestUtils.setField(service, "botLogins", setOf("bot"))
        ReflectionTestUtils.setField(service, "pageSize", pageSize)
        val gitHubContributors = listOf(
            GitHubContributor(
                id = 1,
                login = "login",
                contributions = 10
            )
        )
        every {
            gitHubApiClient.getContributors(organizationName, repositoryName, pageSize)
        } returns gitHubContributors

        val gitHubUsers = mutableListOf<GitHubUser>()
        val contributors = mutableListOf<Contributor>()
        for ((i, gitHubContributor) in gitHubContributors.withIndex()) {
            gitHubUsers.add(
                GitHubUser(
                    id = gitHubContributor.id,
                    login = gitHubContributor.login,
                )
            )
            contributors.add(
                Contributor(
                    id = i.toLong(),
                    name = gitHubContributor.login,
                    contribution = 50
                )
            )
        }
        val savedGithubUser = mutableListOf<com.epam.brn.model.GitHubUser>()
        for ((i, gitHubUser) in gitHubUsers.withIndex()) {
            savedGithubUser.add(
                com.epam.brn.model.GitHubUser(
                    id = gitHubUser.id,
                    login = gitHubUser.login,
                    contributions = gitHubContributors[i].contributions,
                    name = null,
                    email = null,
                    avatarUrl = null,
                    bio = null,
                    company = null
                )
            )
        }

        every { gitHubApiClient.getUser(any()) } returns gitHubUsers[0]
        every { gitHubUserRepository.findById(any()) } returns Optional.empty()
        every { gitHubUserRepository.save(any()) } returnsMany savedGithubUser
        every { contributorRepository.findByGitHubUser(any()) } returns Optional.of(contributors.first())
        every { contributorRepository.save(any()) } returnsMany contributors

        // WHEN
        service.synchronizeContributors()

        // THEN
        verify(exactly = 1) {
            gitHubApiClient.getContributors(any(), any(), any())
        }
        verify(exactly = gitHubContributors.size) {
            gitHubApiClient.getUser(any())
        }
        verify(exactly = gitHubContributors.size) {
            gitHubUserRepository.findById(any())
        }
        verify(exactly = gitHubContributors.size) {
            contributorRepository.findByGitHubUser(any())
        }
        verify(exactly = gitHubContributors.size) {
            contributorRepository.save(any())
        }
    }

    @Test
    fun `should call needed method when don't need update user (only contributors field) from github`() {
        // GIVEN
        val organizationName = "organization"
        val repositoryName = "repository"
        val pageSize = 10
        ReflectionTestUtils.setField(service, "gitHubOrganizationName", organizationName)
        ReflectionTestUtils.setField(service, "gitHubRepositoryName", repositoryName)
        ReflectionTestUtils.setField(service, "botLogins", setOf("bot"))
        ReflectionTestUtils.setField(service, "pageSize", pageSize)
        val gitHubContributors = listOf(
            GitHubContributor(
                id = 1,
                login = "login",
                contributions = 10
            )
        )
        every {
            gitHubApiClient.getContributors(organizationName, repositoryName, pageSize)
        } returns gitHubContributors

        val gitHubUsers = mutableListOf<GitHubUser>()
        val contributors = mutableListOf<Contributor>()
        for ((i, gitHubContributor) in gitHubContributors.withIndex()) {
            gitHubUsers.add(
                GitHubUser(
                    id = gitHubContributor.id,
                    login = gitHubContributor.login,
                )
            )
            contributors.add(
                Contributor(
                    id = i.toLong(),
                    name = gitHubContributor.login,
                    contribution = gitHubContributor.contributions
                )
            )
        }
        val savedGithubUser = mutableListOf<com.epam.brn.model.GitHubUser>()
        for ((i, gitHubUser) in gitHubUsers.withIndex()) {
            savedGithubUser.add(
                com.epam.brn.model.GitHubUser(
                    id = gitHubUser.id,
                    login = gitHubUser.login,
                    contributions = gitHubContributors[i].contributions,
                    name = null,
                    email = null,
                    avatarUrl = null,
                    bio = null,
                    company = null
                )
            )
        }

        every { gitHubApiClient.getUser(any()) } returns gitHubUsers[0]
        every { gitHubUserRepository.findById(any()) } returns Optional.of(savedGithubUser[0])
        every { gitHubUserRepository.save(any()) } returnsMany savedGithubUser
        every { contributorRepository.findByGitHubUser(any()) } returns Optional.of(contributors.first())
        every { contributorRepository.save(any()) } returnsMany contributors

        // WHEN
        service.synchronizeContributors()

        // THEN
        verify(exactly = 1) {
            gitHubApiClient.getContributors(any(), any(), any())
        }
        verify(exactly = gitHubContributors.size) {
            gitHubApiClient.getUser(any())
        }
        verify(exactly = gitHubContributors.size) {
            gitHubUserRepository.findById(any())
        }
        verify(exactly = gitHubContributors.size) {
            contributorRepository.findByGitHubUser(any())
        }
        verify(exactly = 0) {
            contributorRepository.save(any())
        }
    }

    @Test
    fun `should call needed method when user in bot list from github`() {
        // GIVEN
        val organizationName = "organization"
        val repositoryName = "repository"
        val pageSize = 10
        val botLogin = "bot"
        ReflectionTestUtils.setField(service, "gitHubOrganizationName", organizationName)
        ReflectionTestUtils.setField(service, "gitHubRepositoryName", repositoryName)
        ReflectionTestUtils.setField(service, "botLogins", setOf(botLogin))
        ReflectionTestUtils.setField(service, "pageSize", pageSize)
        val gitHubContributors = listOf(
            GitHubContributor(
                id = 1,
                login = botLogin,
                contributions = 10
            )
        )
        every {
            gitHubApiClient.getContributors(organizationName, repositoryName, pageSize)
        } returns gitHubContributors

        // WHEN
        service.synchronizeContributors()

        // THEN
        verify(exactly = 1) {
            gitHubApiClient.getContributors(any(), any(), any())
        }
        verify(exactly = 0) {
            gitHubApiClient.getUser(any())
        }
        verify(exactly = 0) {
            gitHubUserRepository.findById(any())
        }
        verify(exactly = 0) {
            contributorRepository.findByGitHubUser(any())
        }
        verify(exactly = 0) {
            contributorRepository.save(any())
        }
    }
}
