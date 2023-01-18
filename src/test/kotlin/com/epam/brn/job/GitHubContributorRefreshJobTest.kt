package com.epam.brn.job

import com.epam.brn.dto.github.GitHubContributorDto
import com.epam.brn.dto.github.GitHubUserDto
import com.epam.brn.model.Contributor
import com.epam.brn.repo.GitHubUserRepository
import com.epam.brn.service.ContributorService
import com.epam.brn.webclient.GitHubApiClient
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional

private const val s = "repository1"

@ExtendWith(MockKExtension::class)
internal class GitHubContributorRefreshJobTest {

    @InjectMockKs
    private lateinit var service: GitHubContributorRefreshJob

    @MockK
    private lateinit var gitHubApiClient: GitHubApiClient

    @MockK
    private lateinit var gitHubUserRepository: GitHubUserRepository

    @MockK
    private lateinit var contributorService: ContributorService

    @Test
    fun `should call needed method when new user income from github`() {
        // GIVEN
        val organizationName = "organization"
        val repositoryName1 = "repository1"
        val repositoryName2 = "repository2"
        val repositoryNames = setOf(repositoryName1, repositoryName2)
        val pageSize = 10

        ReflectionTestUtils.setField(service, "gitHubOrganizationName", organizationName)
        ReflectionTestUtils.setField(service, "gitHubRepositoryNames", repositoryNames)
        ReflectionTestUtils.setField(service, "botLogins", setOf("bot"))
        ReflectionTestUtils.setField(service, "pageSize", pageSize)

        val gitHubContributorDtos1 = listOf(
            GitHubContributorDto(
                id = 1,
                login = "login",
                contributions = 10
            ),
            GitHubContributorDto(
                id = 2,
                login = "login2",
                contributions = 20
            ),
        )

        val gitHubContributorDtos2 = listOf(
            GitHubContributorDto(
                id = 3,
                login = "login3",
                contributions = 30
            )
        )

        every {
            gitHubApiClient.getGitHubContributors(organizationName, repositoryName1, pageSize)
        } returns gitHubContributorDtos1

        every {
            gitHubApiClient.getGitHubContributors(organizationName, repositoryName2, pageSize)
        } returns gitHubContributorDtos2

        val gitHubUserDtos = mutableListOf<GitHubUserDto>()
        val contributors = mutableListOf<Contributor>()
        for ((i, gitHubContributor) in (gitHubContributorDtos1 + gitHubContributorDtos2).withIndex()) {
            gitHubUserDtos.add(
                GitHubUserDto(
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
        for ((i, gitHubUser) in gitHubUserDtos.withIndex()) {
            savedGithubUser.add(
                com.epam.brn.model.GitHubUser(
                    id = gitHubUser.id,
                    login = gitHubUser.login,
                    contributions = (gitHubContributorDtos1 + gitHubContributorDtos2)[i].contributions,
                    name = null,
                    email = null,
                    avatarUrl = null,
                    bio = null,
                    company = null
                )
            )
        }
        val contributorMockK = mockk<Contributor>()
        every { gitHubApiClient.getGitHubUser(any()) } returns gitHubUserDtos[0]
        every { gitHubUserRepository.findById(any()) } returns Optional.empty()
        every { gitHubUserRepository.save(any()) } returnsMany savedGithubUser
        every { contributorService.createOrUpdateByGitHubUser(any(), any()) } returns contributorMockK

        // WHEN
        service.synchronizeContributors()

        // THEN
        verify(exactly = 2) {
            gitHubApiClient.getGitHubContributors(any(), any(), any())
        }
        verify(exactly = gitHubContributorDtos1.size + gitHubContributorDtos2.size) {
            gitHubApiClient.getGitHubUser(any())
        }
        verify(exactly = gitHubContributorDtos1.size + gitHubContributorDtos2.size) {
            gitHubUserRepository.findById(any())
        }
    }

    @Test
    fun `should call needed method when need update user (only contributors field) from github`() {
        // GIVEN
        val organizationName = "organization"
        val repositoryName = "repository"
        val pageSize = 10
        ReflectionTestUtils.setField(service, "gitHubOrganizationName", organizationName)
        ReflectionTestUtils.setField(service, "gitHubRepositoryNames", setOf(repositoryName))
        ReflectionTestUtils.setField(service, "botLogins", setOf("bot"))
        ReflectionTestUtils.setField(service, "pageSize", pageSize)
        val gitHubContributorDtos = listOf(
            GitHubContributorDto(
                id = 1,
                login = "login",
                contributions = 10
            )
        )
        every {
            gitHubApiClient.getGitHubContributors(organizationName, repositoryName, pageSize)
        } returns gitHubContributorDtos

        val gitHubUserDtos = mutableListOf<GitHubUserDto>()
        val contributors = mutableListOf<Contributor>()
        for ((i, gitHubContributor) in gitHubContributorDtos.withIndex()) {
            gitHubUserDtos.add(
                GitHubUserDto(
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
        for ((i, gitHubUser) in gitHubUserDtos.withIndex()) {
            savedGithubUser.add(
                com.epam.brn.model.GitHubUser(
                    id = gitHubUser.id,
                    login = gitHubUser.login,
                    contributions = gitHubContributorDtos[i].contributions,
                    name = null,
                    email = null,
                    avatarUrl = null,
                    bio = null,
                    company = null
                )
            )
        }
        val contributorMockK = mockk<Contributor>()
        every { gitHubApiClient.getGitHubUser(any()) } returns gitHubUserDtos[0]
        every { gitHubUserRepository.findById(any()) } returns Optional.empty()
        every { gitHubUserRepository.save(any()) } returnsMany savedGithubUser
        every { contributorService.createOrUpdateByGitHubUser(any(), any()) } returns contributorMockK

        // WHEN
        service.synchronizeContributors()

        // THEN
        verify(exactly = 1) {
            gitHubApiClient.getGitHubContributors(any(), any(), any())
        }
        verify(exactly = gitHubContributorDtos.size) {
            gitHubApiClient.getGitHubUser(any())
        }
        verify(exactly = gitHubContributorDtos.size) {
            gitHubUserRepository.findById(any())
        }
        verify(exactly = gitHubContributorDtos.size) {
            contributorService.createOrUpdateByGitHubUser(any(), any())
        }
    }

    @Test
    fun `should call needed method when don't need update user (only contributors field) from github`() {
        // GIVEN
        val organizationName = "organization"
        val repositoryName = "repository"
        val pageSize = 10
        ReflectionTestUtils.setField(service, "gitHubOrganizationName", organizationName)
        ReflectionTestUtils.setField(service, "gitHubRepositoryNames", setOf(repositoryName))
        ReflectionTestUtils.setField(service, "botLogins", setOf("bot"))
        ReflectionTestUtils.setField(service, "pageSize", pageSize)
        val gitHubContributorDtos = listOf(
            GitHubContributorDto(
                id = 1,
                login = "login",
                contributions = 10
            )
        )
        every {
            gitHubApiClient.getGitHubContributors(organizationName, repositoryName, pageSize)
        } returns gitHubContributorDtos

        val gitHubUserDtos = mutableListOf<GitHubUserDto>()
        val contributors = mutableListOf<Contributor>()
        for ((i, gitHubContributor) in gitHubContributorDtos.withIndex()) {
            gitHubUserDtos.add(
                GitHubUserDto(
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
        for ((i, gitHubUser) in gitHubUserDtos.withIndex()) {
            savedGithubUser.add(
                com.epam.brn.model.GitHubUser(
                    id = gitHubUser.id,
                    login = gitHubUser.login,
                    contributions = gitHubContributorDtos[i].contributions,
                    name = null,
                    email = null,
                    avatarUrl = null,
                    bio = null,
                    company = null
                )
            )
        }
        val contributorMockK = mockk<Contributor>()
        every { gitHubApiClient.getGitHubUser(any()) } returns gitHubUserDtos[0]
        every { gitHubUserRepository.findById(any()) } returns Optional.of(savedGithubUser[0])
        every { gitHubUserRepository.save(any()) } returnsMany savedGithubUser
        every { contributorService.createOrUpdateByGitHubUser(any(), any()) } returns contributorMockK

        // WHEN
        service.synchronizeContributors()

        // THEN
        verify(exactly = 1) {
            gitHubApiClient.getGitHubContributors(any(), any(), any())
        }
        verify(exactly = gitHubContributorDtos.size) {
            gitHubApiClient.getGitHubUser(any())
        }
        verify(exactly = gitHubContributorDtos.size) {
            gitHubUserRepository.findById(any())
        }
        verify(exactly = gitHubContributorDtos.size) {
            contributorService.createOrUpdateByGitHubUser(any(), any())
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
        ReflectionTestUtils.setField(service, "gitHubRepositoryNames", setOf(repositoryName))
        ReflectionTestUtils.setField(service, "botLogins", setOf(botLogin))
        ReflectionTestUtils.setField(service, "pageSize", pageSize)
        val gitHubContributorDtos = listOf(
            GitHubContributorDto(
                id = 1,
                login = botLogin,
                contributions = 10
            )
        )
        every {
            gitHubApiClient.getGitHubContributors(organizationName, repositoryName, pageSize)
        } returns gitHubContributorDtos

        // WHEN
        service.synchronizeContributors()

        // THEN
        verify(exactly = 1) {
            gitHubApiClient.getGitHubContributors(any(), any(), any())
        }
        verify(exactly = 0) {
            gitHubApiClient.getGitHubUser(any())
        }
        verify(exactly = 0) {
            gitHubUserRepository.findById(any())
        }
        verify(exactly = 0) {
            contributorService.createOrUpdateByGitHubUser(any(), any())
        }
    }
    @Test
    fun `should call needed method when repository list is empty`() {
        // GIVEN
        val organizationName = "organization"
        val pageSize = 10
        val botLogin = "bot"
        val repositories = emptySet<Any>()
        ReflectionTestUtils.setField(service, "gitHubOrganizationName", organizationName)
        ReflectionTestUtils.setField(service, "gitHubRepositoryNames", repositories)
        ReflectionTestUtils.setField(service, "botLogins", setOf(botLogin))
        ReflectionTestUtils.setField(service, "pageSize", pageSize)

        // WHEN
        service.synchronizeContributors()

        // THEN
        verify(exactly = 0) {
            gitHubApiClient.getGitHubContributors(any(), any(), any())
        }
        verify(exactly = 0) {
            gitHubApiClient.getGitHubUser(any())
        }
        verify(exactly = 0) {
            gitHubUserRepository.findById(any())
        }
        verify(exactly = 0) {
            contributorService.createOrUpdateByGitHubUser(any(), any())
        }
    }
}
