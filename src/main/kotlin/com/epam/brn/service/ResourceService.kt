package com.epam.brn.service

import com.epam.brn.dto.ResourceDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Resource
import com.epam.brn.repo.ResourceRepository
import org.springframework.data.repository.findByIdOrNull
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

    fun updateDescription(id: Long, description: String): ResourceDto {
        return resourceRepository.findByIdOrNull(id)?.let {
            it.description = description
            resourceRepository.save(it)
            it.toDto()
        } ?: throw EntityNotFoundException("Resource not found by id=$id")
    }
}
