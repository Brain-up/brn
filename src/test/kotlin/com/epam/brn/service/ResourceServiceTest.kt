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
    lateinit var resourceRepositoryMock: ResourceRepository

    @MockK
    lateinit var resourceMock: Resource

    val id = 1L
    val description = "I'd like to add a description"

    @Test
    fun `should return first resource by word if not empty`() {
        // GIVEN
        val word = "word"
        every { resourceRepositoryMock.findByWordLike(word) } returns listOf(resourceMock)

        // WHEN
        val foundFirstResource = resourceService.findFirstResourceByWordLike(word)

        // THEN
        foundFirstResource shouldBe resourceMock
    }

    @Test
    fun `should return null if the word is not found`() {
        // GIVEN
        val word = "word"
        every { resourceRepositoryMock.findByWordLike(word) } returns emptyList()

        // WHEN
        val foundFirstResource = resourceService.findFirstResourceByWordLike(word)

        // THEN
        foundFirstResource shouldBe null
    }

    @Test
    fun `should return resource by word and audio file url`() {
        // GIVEN
        val word = "word"
        val audioFileName = "audioFileName"
        every { resourceRepositoryMock.findFirstByWordAndAudioFileUrlLike(word, audioFileName) } returns Optional.of(
            resourceMock
        )

        // WHEN
        val foundResource = resourceService.findFirstByWordAndAudioFileUrlLike(word, audioFileName)

        // THEN
        foundResource shouldBe resourceMock
    }

    @Test
    fun `should return null if word and audio file url is not found`() {
        // GIVEN
        val word = "word"
        val audioFileName = "audioFileName"
        every { resourceRepositoryMock.findFirstByWordAndAudioFileUrlLike(word, audioFileName) } returns Optional.empty()

        // WHEN
        val foundResource = resourceService.findFirstByWordAndAudioFileUrlLike(word, audioFileName)

        // THEN
        foundResource shouldBe null
    }

    @Test
    fun `should save resource`() {
        // GIVEN
        every { resourceRepositoryMock.save(resourceMock) } returns resourceMock

        // WHEN
        val result = resourceService.save(resourceMock)

        // THEN
        result shouldBe resourceMock
    }

    @Test
    fun `should update description successfully`() {
        // GIVEN
        val resource = Resource(
            id = id,
            wordType = "OBJECT"
        )
        every { resourceRepositoryMock.findByIdOrNull(id) } returns resource
        every { resourceRepositoryMock.save(resource) } returns resource

        // WHEN
        val result: ResourceDto = resourceService.updateDescription(id, description)

        // THEN
        verify(exactly = 1) { resourceRepositoryMock.findByIdOrNull(id) }
        verify(exactly = 1) { resourceRepositoryMock.save(resource) }

        id shouldBe result.id
        description shouldBe result.description
    }

    @Test
    fun `Should throw EntityNotFoundException if it does not exist`() {
        // GIVEN
        val expectedErrorMessage = "Resource not found by id=$id"
        every { resourceRepositoryMock.findByIdOrNull(id) } returns null

        // WHEN
        val exception = assertThrows<EntityNotFoundException> {
            resourceService.updateDescription(id, description)
        }

        // THEN
        expectedErrorMessage shouldBe exception.message
        verify(exactly = 1) { resourceRepositoryMock.findByIdOrNull(id) }
    }
}
