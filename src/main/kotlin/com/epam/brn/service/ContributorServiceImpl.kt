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
import java.util.stream.Collectors
import kotlin.streams.toList

@Service
class ContributorServiceImpl(
    val contributorRepository: ContributorRepository,
) : ContributorService {

    @Transactional(readOnly = true)
    override fun getAllContributors(): List<ContributorResponse> {
        return contributorRepository.findAll().stream()
            .map { e -> e.toContributorDto() }
            .toList()
    }

    @Transactional(readOnly = true)
    override fun getContributors(locale: String, type: ContributorType): List<ContributorResponse> {
        return contributorRepository.findAllByType(type).stream()
            .map { e -> e.toContributorDto(locale) }
            .collect(Collectors.toList())
    }

    @Transactional
    override fun createContributor(dto: ContributorRequest): ContributorResponse {
        return contributorRepository.save(dto.toEntity()).toContributorDto()
    }

    @Transactional
    override fun updateContributor(id: Long, dto: ContributorRequest): ContributorResponse {
        val contributor = contributorRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Contributor with id=$id was not found") }
        contributor.name = dto.name
        contributor.nameEn = dto.nameEn
        contributor.description = dto.description
        contributor.descriptionEn = dto.descriptionEn
        contributor.company = dto.company
        contributor.companyEn = dto.companyEn
        contributor.contribution = dto.contribution!!
        contributor.pictureUrl = dto.pictureUrl
        contributor.type = dto.type!!
        contributor.contacts.clear()
        contributor.contacts.addAll(dto.contacts.map { it.toEntity() }.toMutableSet())
        return contributorRepository.save(contributor).toContributorDto()
    }

    @Transactional
    override fun createOrUpdateByGitHubUser(gitHubUser: GitHubUser): Contributor {
        val existContributor = contributorRepository.findByGitHubUser(gitHubUser)
        return existContributor?.updateByGitHubUser(gitHubUser) ?: createContributor(gitHubUser)
    }

    private fun createContributor(gitHubUser: GitHubUser): Contributor {
        val contributor = Contributor(contribution = gitHubUser.contributions)
        contributor.gitHubUser = gitHubUser
        contributor.name = gitHubUser.name
        contributor.company = gitHubUser.company
        contributor.pictureUrl = gitHubUser.avatarUrl
        contributor.description = gitHubUser.bio
        gitHubUser.email?.let { email ->
            contributor.contacts.add(Contact(value = email))
        }
        return contributorRepository.save(contributor)
    }

    private fun Contributor.updateByGitHubUser(gitHubUser: GitHubUser): Contributor {
        if (this.contribution != gitHubUser.contributions)
            this.contribution = gitHubUser.contributions
        if (this.name.isNullOrEmpty())
            this.name = gitHubUser.name
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
