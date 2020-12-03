package com.epam.brn.integration.repo

import com.epam.brn.model.Authority
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthorityRepository : JpaRepository<Authority, Long> {
    fun findAuthorityByAuthorityName(authorityName: String): Authority?
    fun findAuthoritiesById(id: Long): Authority?
}
