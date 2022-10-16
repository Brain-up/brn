package com.epam.brn.service.scheduler

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

    @Scheduled(cron = "\${github.contributors.sync.cron}")
    @Transactional
    fun synchronizeContributors() {
        println("$gitHubOrganizationName, $gitHubRepositoryName, $pageSize")
        val contributors = gitHubApiClient.getContributors(gitHubOrganizationName, gitHubRepositoryName, pageSize)

        contributors.forEach {
            val user = gitHubApiClient.getUser(it.login)

            user?.apply {
                if (botLogins.contains(this.login).not()) {
                    val foundedGitHubUser = gitHubUserRepository.findById(this.id)
                    val gitHubUser: GitHubUser
                    if (foundedGitHubUser.isPresent) {
                        gitHubUser = foundedGitHubUser.get()
                        gitHubUser.let { usr ->
                            if (usr.name != this.name)
                                usr.name = this.name
                            if (usr.login != this.login)
                                usr.login = this.login
                            if (usr.email != this.email)
                                usr.email = this.email
                            if (usr.avatarUrl != this.avatarUrl)
                                usr.avatarUrl = this.avatarUrl
                            if (usr.bio != this.bio)
                                usr.bio = this.bio
                            if (usr.company != this.company)
                                usr.company = this.company
                            if (usr.contributions != it.contributions)
                                usr.contributions = it.contributions
                        }
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
}
