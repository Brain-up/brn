package com.epam.brn.service

import com.epam.brn.model.Authority

interface AuthorityService {

    fun findAuthorityById(authorityId: Long): Authority
    fun findAuthorityByAuthorityName(authorityName: String): Authority
    fun save(authority: Authority): Authority
}
