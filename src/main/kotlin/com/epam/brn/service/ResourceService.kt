package com.epam.brn.service

import com.epam.brn.model.Resource
import com.epam.brn.repo.ResourceRepository
import org.springframework.stereotype.Service

@Service
class ResourceService(private val resourceRepository: ResourceRepository) {

    fun findFirstResourceByWordLike(word: String): Resource? {
        val resources = resourceRepository.findByWordLike(word)
        return if (resources.isNotEmpty()) resources.first() else null
    }

    fun findFirstByWordAndAudioFileUrlLike(word: String, audioFileName: String): Resource? {
        return resourceRepository
            .findFirstByWordAndAudioFileUrlLike(word, audioFileName)
            .orElse(null)
    }

    fun save(resource: Resource): Resource = resourceRepository.save(resource)
}
