package com.epam.brn.service

import com.epam.brn.dto.ContributorUserDto
import com.epam.brn.model.ContributorType

interface ContributorServer {

    fun getContributors(locale: String, type: ContributorType): List<ContributorUserDto>
}
