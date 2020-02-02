package com.epam.brn.service

import com.epam.brn.model.Authority

interface AuthorityService {

    fun findAuthorityById(authorityId: Long): List<Authority>
    fun findAuthorityByAuthorityName(authorityName: String): Authority
    fun save(authority: Authority): Authority
}
