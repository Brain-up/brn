package com.epam.brn.service

import com.epam.brn.exception.EntityNotFoundException
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

    fun findByWordAndAudioFileUrlLike(word: String, audioFileUrl: String): List<Resource> {
        return resourceRepository.findByWordAndAudioFileUrlLike(word, audioFileUrl)
    }

    fun save(resource: Resource): Resource {
        return resourceRepository.save(resource)
    }

    fun saveAll(resources: List<Resource>): MutableIterable<Resource> {
        return resourceRepository.saveAll(resources)
    }

    fun findById(id: Long): Resource {
        return resourceRepository.findById(id)
            .orElseThrow { EntityNotFoundException("no resource was found for id=$id") }
    }
}
