package com.epam.brn.service

import com.epam.brn.model.Resource
import com.epam.brn.repo.ResourceRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ResourceService(@Autowired val resourceRepository: ResourceRepository) {
    private val log = logger()

    fun findByWordLike(word: String): List<Resource> {
        return resourceRepository.findByWordLike(word)
    }

    fun save(resource: Resource) {
        resourceRepository.save(resource)
    }
}