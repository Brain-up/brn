package com.epam.brn.service

import com.epam.brn.dto.ResourceDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Resource
import com.epam.brn.repo.ResourceRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class ResourceServiceTest {

    @InjectMockKs
    lateinit var resourceService: ResourceService

    @MockK
    lateinit var resourceRepository: ResourceRepository

    @MockK
    lateinit var resource: Resource

    val id = 1L
    val description = "I'd like to add a description"

    @Test
    fun `should return first resource by word if not empty`() {
        // GIVEN
        val word = "word"
        every { resourceRepository.findByWordLike(word) } returns listOf(resource)

        // WHEN
        val foundFirstResource = resourceService.findFirstResourceByWordLike(word)

        // THEN
        foundFirstResource.shouldBe(resource)
    }

    @Test
    fun `should return null if the word is not found`() {
        // GIVEN
        val word = "word"
        every { resourceRepository.findByWordLike(word) } returns emptyList()

        // WHEN
        val foundFirstResource = resourceService.findFirstResourceByWordLike(word)

        // THEN
        foundFirstResource.shouldBeNull()
    }

    @Test
    fun `should return resource by word and audio file url`() {
        // GIVEN
        val word = "word"
        val audioFileName = "audioFileName"
        every { resourceRepository.findFirstByWordAndAudioFileUrlLike(word, audioFileName) } returns Optional.of(resource)

        // WHEN
        val foundResource = resourceService.findFirstByWordAndAudioFileUrlLike(word, audioFileName)

        // THEN
        foundResource.shouldBe(resource)
    }

    @Test
    fun `should return null if word and audio file url is not found`() {
        // GIVEN
        val word = "word"
        val audioFileName = "audioFileName"
        every { resourceRepository.findFirstByWordAndAudioFileUrlLike(word, audioFileName) } returns Optional.empty()

        // WHEN
        val foundResource = resourceService.findFirstByWordAndAudioFileUrlLike(word, audioFileName)

        // THEN
        foundResource.shouldBeNull()
    }

    @Test
    fun `should save resource`() {
        // GIVEN
        every { resourceRepository.save(resource) } returns resource

        // WHEN
        val result = resourceService.save(resource)

        // THEN
        result.shouldBe(resource)
    }

    @Test
    fun `should update description successfully`() {
        // GIVEN
        val resource = Resource(
            id = id,
            wordType = "OBJECT"
        )
        every { resourceRepository.findByIdOrNull(id) } returns resource
        every { resourceRepository.save(resource) } returns resource

        // WHEN
        val result: ResourceDto = resourceService.updateDescription(id, description)

        // THEN
        verify(exactly = 1) { resourceRepository.findByIdOrNull(id) }
        verify(exactly = 1) { resourceRepository.save(resource) }

        id.shouldBe(result.id)
        description.shouldBe(result.description)
    }

    @Test
    fun `Should throw EntityNotFoundException if it does not exist`() {
        // GIVEN
        val expectedErrorMessage = "Resource not found by id=$id"
        every { resourceRepository.findByIdOrNull(id) } returns null

        // WHEN
        val exception = assertThrows<EntityNotFoundException> {
            resourceService.updateDescription(id, description)
        }

        // THEN
        expectedErrorMessage.shouldBe(exception.message)
        verify(exactly = 1) { resourceRepository.findByIdOrNull(id) }
    }
}
