package com.epam.brn.job

import com.epam.brn.dto.github.GitHubContributorDto
import com.epam.brn.dto.github.GitHubUserDto
import com.epam.brn.model.Contact
import com.epam.brn.model.Contributor
import com.epam.brn.model.GitHubUser
import com.epam.brn.repo.ContributorRepository
import com.epam.brn.repo.GitHubUserRepository
import com.epam.brn.webclient.GitHubApiClient
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Service
class GitHubContributorRefreshJob(
    val gitHubApiClient: GitHubApiClient,
    val gitHubUserRepository: GitHubUserRepository,
    val contributorRepository: ContributorRepository,
) {

    private val log = logger()

    @Value("\${github.contributors.sync.organization-name}")
    private val gitHubOrganizationName: String = ""

    @Value("\${github.contributors.sync.repository-name}")
    private val gitHubRepositoryName: String = ""

    @Value("#{'\${github.contributors.bot-logins}'.split(',')}")
    private lateinit var botLogins: Set<String>

    @Value("\${github.contributors.default-page-size}")
    private val pageSize: Int = 30

    @PostConstruct
    @Transactional
    fun runOnceAtStartup() {
        try {
            if (gitHubUserRepository.count() <= 0)
                synchronizeContributors()
        } catch (e: Exception) {
            log.error("Some error occurred: ${e.message}", e)
        }
    }

    @Scheduled(cron = "\${github.contributors.sync.cron}")
    @Transactional
    fun synchronizeContributors() {
        val gitHubContributors: List<GitHubContributorDto> = gitHubApiClient
            .getGitHubContributors(gitHubOrganizationName, gitHubRepositoryName, pageSize)
            .filter { !botLogins.contains(it.login) }

        gitHubContributors.forEach { gitHubContributor ->
            val gitHubUserDto: GitHubUserDto = gitHubApiClient.getGitHubUser(gitHubContributor.login)!!
            val existGitHubUser = gitHubUserRepository.findById(gitHubUserDto.id)
            val updatedGitHubUser = if (existGitHubUser.isPresent)
                updateGitHubUser(gitHubUserDto, existGitHubUser.get(), gitHubContributor)
            else
                createGitHubUser(gitHubUserDto, gitHubContributor)
            updateOrCreateContributor(updatedGitHubUser)
        }
    }

    private fun updateOrCreateContributor(gitHubUser: GitHubUser) {
        val existContributor = contributorRepository.findByGitHubUser(gitHubUser)
        if (existContributor.isEmpty) {
            val contributor = Contributor(contribution = gitHubUser.contributions)
            contributor.gitHubUser = gitHubUser
            gitHubUser.email?.let { email ->
                contributor.contacts.add(
                    Contact(value = email)
                )
            }
            contributorRepository.save(contributor)
        } else {
            val contributor = existContributor.get()
            if (contributor.contribution != gitHubUser.contributions) {
                contributor.contribution = gitHubUser.contributions
                contributorRepository.save(contributor)
            }
        }
    }

    private fun createGitHubUser(gitHubUserDto: GitHubUserDto, gitHubContributorDto: GitHubContributorDto): GitHubUser {
        return gitHubUserRepository.save(gitHubUserDto.toEntity(gitHubContributorDto.contributions))
    }

    private fun updateGitHubUser(
        gitHubUserDto: GitHubUserDto,
        gitHubUser: GitHubUser,
        gitHubContributorDto: GitHubContributorDto
    ): GitHubUser {
        gitHubUser.let { usr ->
            if (usr.name != gitHubUserDto.name)
                usr.name = gitHubUserDto.name
            if (usr.login != gitHubUserDto.login)
                usr.login = gitHubUserDto.login
            if (usr.email != gitHubUserDto.email)
                usr.email = gitHubUserDto.email
            if (usr.avatarUrl != gitHubUserDto.avatarUrl)
                usr.avatarUrl = gitHubUserDto.avatarUrl
            if (usr.bio != gitHubUserDto.bio)
                usr.bio = gitHubUserDto.bio
            if (usr.company != gitHubUserDto.company)
                usr.company = gitHubUserDto.company
            if (usr.contributions != gitHubContributorDto.contributions)
                usr.contributions = gitHubContributorDto.contributions
        }
        return gitHubUserRepository.save(gitHubUser)
    }
}
