package com.epam.brn.auth

import com.epam.brn.model.Authority

interface AuthorityService {

    fun findAuthorityById(authorityId: Long): Authority
    fun findAuthorityByAuthorityName(authorityName: String): Authority
    fun findAll(): List<Authority>
    fun save(authority: Authority): Authority
}
