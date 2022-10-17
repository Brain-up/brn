package com.epam.brn.job

import com.epam.brn.dto.github.GitHubContributorDto
import com.epam.brn.dto.github.GitHubUserDto
import com.epam.brn.model.GitHubUser
import com.epam.brn.repo.GitHubUserRepository
import com.epam.brn.service.ContributorService
import com.epam.brn.webclient.GitHubApiClient
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import javax.annotation.PostConstruct

@Service
class GitHubContributorRefreshJob(
    val gitHubApiClient: GitHubApiClient,
    val gitHubUserRepository: GitHubUserRepository,
    val contributorService: ContributorService,
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
            log.error("Some error occurred on getting gitHub users: ${e.message}", e)
        }
    }

    @Scheduled(cron = "\${github.contributors.sync.cron}")
    @Transactional
    fun synchronizeContributors() {
        gitHubApiClient
            .getGitHubContributors(gitHubOrganizationName, gitHubRepositoryName, pageSize)
            .filter { !botLogins.contains(it.login) }
            .forEach { gitHubContributor ->
                val gitHubUserDto: GitHubUserDto = gitHubApiClient.getGitHubUser(gitHubContributor.login)!!
                val existGitHubUser: Optional<GitHubUser> = gitHubUserRepository.findById(gitHubUserDto.id)
                val savedGitHubUser: GitHubUser = if (existGitHubUser.isPresent)
                    updateGitHubUser(gitHubUserDto, existGitHubUser.get(), gitHubContributor)
                else
                    createGitHubUser(gitHubUserDto, gitHubContributor)
                contributorService.createOrUpdateByGitHubUser(savedGitHubUser)
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
