package com.epam.brn.service

import com.epam.brn.dto.request.contributor.ContributorRequest
import com.epam.brn.dto.response.ContributorResponse
import com.epam.brn.enums.ContributorType
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Contact
import com.epam.brn.model.Contributor
import com.epam.brn.model.GitHubUser
import com.epam.brn.repo.ContributorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.streams.toList

@Service
class ContributorServiceImpl(
    val contributorRepository: ContributorRepository,
) : ContributorService {

    @Transactional(readOnly = true)
    override fun getAllContributors(): List<ContributorResponse> {
        return contributorRepository.findAll().stream()
            .map { e -> e.toContributorResponse() }
            .toList()
    }

    @Transactional(readOnly = true)
    override fun getContributors(locale: String, type: ContributorType): List<ContributorResponse> {
        return contributorRepository.findAllByType(type).stream()
            .map { e -> e.toContributorResponse(locale) }
            .toList()
    }

    @Transactional
    override fun createContributor(request: ContributorRequest): ContributorResponse =
        contributorRepository.save(request.toEntity()).toContributorResponse()

    @Transactional
    override fun updateContributor(id: Long, contributorRequest: ContributorRequest): ContributorResponse {
        val contributor = contributorRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Contributor with id=$id was not found") }
        contributor.name = contributorRequest.name
        contributor.nameEn = contributorRequest.nameEn
        contributor.description = contributorRequest.description
        contributor.descriptionEn = contributorRequest.descriptionEn
        contributor.company = contributorRequest.company
        contributor.companyEn = contributorRequest.companyEn
        contributor.contribution = contributorRequest.contribution!!
        contributor.pictureUrl = contributorRequest.pictureUrl
        contributor.type = contributorRequest.type!!
        contributor.contacts.clear()
        contributor.contacts.addAll(contributorRequest.contacts.map { it.toEntity() }.toMutableSet())
        return contributorRepository.save(contributor).toContributorResponse()
    }

    @Transactional
    override fun createOrUpdateByGitHubUser(gitHubUser: GitHubUser): Contributor {
        val existContributor = contributorRepository.findByGitHubUser(gitHubUser)
        return existContributor
            ?.updateByGitHubUser(gitHubUser)
            ?: createContributor(gitHubUser)
    }

    private fun createContributor(gitHubUser: GitHubUser): Contributor {
        val contributor = Contributor(
            contribution = gitHubUser.contributions,
            name = gitHubUser.name ?: "gitHubNick:${gitHubUser.login}",
            company = gitHubUser.company,
            pictureUrl = gitHubUser.avatarUrl,
            description = gitHubUser.bio
        )
        contributor.gitHubUser = gitHubUser
        gitHubUser.email?.let { email ->
            contributor.contacts.add(Contact(value = email))
        }
        return contributorRepository.save(contributor)
    }

    private fun Contributor.updateByGitHubUser(gitHubUser: GitHubUser): Contributor {
        if (this.contribution != gitHubUser.contributions)
            this.contribution = gitHubUser.contributions
        if (this.name.isNullOrEmpty() || this.name != gitHubUser.name)
            this.name = gitHubUser.name ?: gitHubUser.login
        if (this.company.isNullOrEmpty())
            this.company = gitHubUser.company
        if (this.description.isNullOrEmpty())
            this.description = gitHubUser.bio
        if (this.pictureUrl.isNullOrEmpty())
            this.pictureUrl = gitHubUser.avatarUrl
        if (this.contacts.isEmpty() && gitHubUser.email != null)
            this.contacts.add(Contact(value = gitHubUser.email!!))
        return contributorRepository.save(this)
    }
}
