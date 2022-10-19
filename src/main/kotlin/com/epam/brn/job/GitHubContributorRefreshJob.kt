package com.epam.brn.job

import com.epam.brn.dto.github.GitHubContributorDto
import com.epam.brn.dto.github.GitHubUserDto
import com.epam.brn.model.GitHubUser
import com.epam.brn.repo.GitHubUserRepository
import com.epam.brn.service.ContributorService
import com.epam.brn.webclient.GitHubApiClient
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

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

    @EventListener(ApplicationReadyEvent::class)
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
        log.info("Start sync contributors from GitHub.")
        var gitHubContributorsCount: Int
        var createdContributorsCount = 0
        var updatedContributorsCount = 0
        gitHubApiClient
            .getGitHubContributors(gitHubOrganizationName, gitHubRepositoryName, pageSize)
            .also { log.info("From GitHub repo was got ${it.size} contributors.") }
            .filter { !botLogins.contains(it.login) }
            .also { gitHubContributorsCount = it.count() }
            .forEach { gitHubContributor ->
                val gitHubUserDto: GitHubUserDto = gitHubApiClient.getGitHubUser(gitHubContributor.login)!!
                log.debug("Gotten user from GitHubApi $gitHubUserDto")
                val existGitHubUser: Optional<GitHubUser> = gitHubUserRepository.findById(gitHubUserDto.id)
                log.debug("User in database $existGitHubUser")
                val savedGitHubUser: GitHubUser = if (existGitHubUser.isPresent) {
                    updatedContributorsCount++
                    updateGitHubUser(gitHubUserDto, existGitHubUser.get(), gitHubContributor)
                } else {
                    createdContributorsCount++
                    createGitHubUser(gitHubUserDto, gitHubContributor)
                }
                val contributor = contributorService.createOrUpdateByGitHubUser(savedGitHubUser)
                log.debug("Created\\Updated contributor in database $contributor")
            }
        log.info(
            "GitHubUsers synchronization job end: " +
                "gitHubContributorsCount=$gitHubContributorsCount, " +
                "createdContributorsCount=$createdContributorsCount, " +
                "updatedContributorsCount=$updatedContributorsCount."
        )
    }

    private fun createGitHubUser(gitHubUserDto: GitHubUserDto, gitHubContributorDto: GitHubContributorDto): GitHubUser =
        gitHubUserRepository.save(gitHubUserDto.toEntity(gitHubContributorDto.contributions))

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
