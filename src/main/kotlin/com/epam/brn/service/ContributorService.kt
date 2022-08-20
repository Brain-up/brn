package com.epam.brn.service

import com.epam.brn.dto.response.ContributorResponse
import com.epam.brn.enums.ContributorType

interface ContributorService {

    fun getContributors(locale: String, type: ContributorType): List<ContributorResponse>
}
