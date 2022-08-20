package com.epam.brn.service

import com.epam.brn.dto.response.ContributorResponse
import com.epam.brn.enums.ContributorType
import com.epam.brn.repo.ContributorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

@Service
class GitHubContributorService(
    val contributorRepository: ContributorRepository
) : ContributorService {

    @Transactional(readOnly = true)
    override fun getContributors(locale: String, type: ContributorType): List<ContributorResponse> {
        return contributorRepository.findAllByType(type).stream()
            .map { e -> e.toContributorDto(locale) }
            .collect(Collectors.toList())
    }
}
