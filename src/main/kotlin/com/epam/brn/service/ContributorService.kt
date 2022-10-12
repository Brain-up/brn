package com.epam.brn.service

import com.epam.brn.dto.request.contributor.ContributorRequest
import com.epam.brn.dto.response.ContributorResponse
import com.epam.brn.enums.ContributorType

interface ContributorService {
    fun getContributors(locale: String, type: ContributorType): List<ContributorResponse>
    fun getAllContributors(): List<ContributorResponse>
    fun createContributor(dto: ContributorRequest): ContributorResponse
    fun updateContributor(id: Long, dto: ContributorRequest): ContributorResponse
}
