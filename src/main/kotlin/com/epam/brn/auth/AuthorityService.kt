package com.epam.brn.auth

import com.epam.brn.dto.response.AuthorityDto
import com.epam.brn.model.Authority

interface AuthorityService {

    fun findAuthorityById(authorityId: Long): Authority
    fun findAuthorityByAuthorityName(authorityName: String): Authority
    fun findAll(): List<AuthorityDto>
    fun save(authority: Authority): Authority
}
