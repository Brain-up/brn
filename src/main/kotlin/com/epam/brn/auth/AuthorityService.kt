package com.epam.brn.auth

import com.epam.brn.dto.response.AuthorityResponse
import com.epam.brn.enums.AuthorityType
import com.epam.brn.model.Authority

interface AuthorityService {

    fun findAuthorityById(authorityId: Long): Authority
    fun findAuthorityByAuthorityName(authorityName: String): Authority
    fun findAll(): List<AuthorityResponse>
    fun save(authority: Authority): Authority
    fun hasAuthority(authorityType: AuthorityType): Boolean
}
