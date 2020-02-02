package com.epam.brn.service.impl

import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.model.Authority
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.service.AuthorityService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class AuthorityServiceImpl(private val authorityRepository: AuthorityRepository) :
    AuthorityService {

    private val log = logger()

    override fun findAuthorityById(authorityId: Long): List<Authority> {
        log.debug("getting the authority with id=$authorityId")
        return authorityRepository.findAuthoritiesById(authorityId)
    }

    override fun findAuthorityByAuthorityName(authorityName: String): Authority {
        log.debug("getting the authority with authorityName=$authorityName")
        return authorityRepository.findAuthorityByAuthorityName(authorityName)
            ?: throw NoDataFoundException("Authority with name = $authorityName is not found")
    }

    override fun save(authority: Authority) = authorityRepository.save(authority)
}
