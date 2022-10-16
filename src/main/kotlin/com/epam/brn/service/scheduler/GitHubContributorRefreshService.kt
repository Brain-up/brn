package com.epam.brn.service.scheduler

import com.epam.brn.model.Contact
import com.epam.brn.model.Contributor
import com.epam.brn.model.GitHubUser
import com.epam.brn.repo.ContributorRepository
import com.epam.brn.repo.GitHubUserRepository
import com.epam.brn.webclient.GitHubApiClient
import com.epam.brn.webclient.model.GitHubContributor
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Service
class GitHubContributorRefreshService(
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
        if (gitHubUserRepository.count() <= 0) {
            synchronizeContributors()
        }
    }

    @Scheduled(cron = "\${github.contributors.sync.cron}")
    @Transactional
    fun synchronizeContributors() {
        val contributors = gitHubApiClient.getContributors(gitHubOrganizationName, gitHubRepositoryName, pageSize)

        contributors.forEach {
            if (botLogins.contains(it.login).not()) {
                val user = gitHubApiClient.getUser(it.login)

                user?.apply {
                    val foundedGitHubUser = gitHubUserRepository.findById(this.id)
                    val gitHubUser: GitHubUser
                    if (foundedGitHubUser.isPresent) {
                        gitHubUser = foundedGitHubUser.get()
                        updateGitHubUser(this, gitHubUser, it)
                    } else {
                        gitHubUser = GitHubUser(
                            id = this.id,
                            name = this.name,
                            login = this.login,
                            email = this.email,
                            avatarUrl = this.avatarUrl,
                            bio = this.bio,
                            company = this.company,
                            contributions = it.contributions
                        )
                    }
                    val savedGitHubUser = gitHubUserRepository.save(gitHubUser)
                    val foundedContributor = contributorRepository.findByGitHubUser(savedGitHubUser)
                    if (foundedContributor.isEmpty) {
                        val contributor = Contributor(
                            contribution = gitHubUser.contributions
                        )
                        contributor.gitHubUser = savedGitHubUser
                        savedGitHubUser.email?.let { email ->
                            contributor.contacts.add(
                                Contact(value = email)
                            )
                        }
                        contributorRepository.save(contributor)
                    } else {
                        val contributor = foundedContributor.get()
                        if (contributor.contribution != gitHubUser.contributions) {
                            contributor.contribution = gitHubUser.contributions
                            contributorRepository.save(contributor)
                        }
                    }
                }
            }
        }
    }

    private fun updateGitHubUser(
        gitHubUserClient: com.epam.brn.webclient.model.GitHubUser,
        gitHubUser: GitHubUser,
        gitHubContributor: GitHubContributor
    ) {
        gitHubUser.let { usr ->
            if (usr.name != gitHubUserClient.name)
                usr.name = gitHubUserClient.name
            if (usr.login != gitHubUserClient.login)
                usr.login = gitHubUserClient.login
            if (usr.email != gitHubUserClient.email)
                usr.email = gitHubUserClient.email
            if (usr.avatarUrl != gitHubUserClient.avatarUrl)
                usr.avatarUrl = gitHubUserClient.avatarUrl
            if (usr.bio != gitHubUserClient.bio)
                usr.bio = gitHubUserClient.bio
            if (usr.company != gitHubUserClient.company)
                usr.company = gitHubUserClient.company
            if (usr.contributions != gitHubContributor.contributions)
                usr.contributions = gitHubContributor.contributions
        }
    }
}
