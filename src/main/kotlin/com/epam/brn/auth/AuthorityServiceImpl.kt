package com.epam.brn.auth

import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.integration.repo.AuthorityRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class AuthorityServiceImpl(private val authorityRepository: AuthorityRepository) :
    AuthorityService {

    private val log = logger()

    override fun findAuthorityById(authorityId: Long): Authority {
        log.debug("getting the authority with authorityId=$authorityId")
        return authorityRepository.findAuthoritiesById(authorityId)
            ?: throw EntityNotFoundException("Authority with authorityId = $authorityId is not found")
    }

    override fun findAuthorityByAuthorityName(authorityName: String): Authority {
        log.debug("getting the authority with authorityName=$authorityName")
        return authorityRepository.findAuthorityByAuthorityName(authorityName)
            ?: throw EntityNotFoundException("Authority with name = $authorityName is not found")
    }

    override fun save(authority: Authority) = authorityRepository.save(authority)

    override fun findAll(): List<Authority> = authorityRepository.findAll()
}
