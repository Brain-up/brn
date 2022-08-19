package com.epam.brn.service

import com.epam.brn.dto.ContributorUserDto
import com.epam.brn.enums.ContributorType
import com.epam.brn.repo.ContributorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

@Service
class GitHubContributorService(
    val contributorRepository: ContributorRepository
) : ContributorServer {

    @Transactional(readOnly = true)
    override fun getContributors(locale: String, type: ContributorType): List<ContributorUserDto> {
        return contributorRepository.findAllByType(type).stream()
            .map { e -> e.toContributorUserDto(locale) }
            .collect(Collectors.toList())
    }
}
