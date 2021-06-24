package com.epam.brn.service

import com.epam.brn.dto.ResourceDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Resource
import com.epam.brn.repo.ResourceRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class ResourceServiceTest {

    @InjectMockKs
    lateinit var resourceService: ResourceService

    @MockK
    lateinit var resourceRepository: ResourceRepository

    val id = 1L
    val description = "I'd like to add a description"

    @Test
    fun `should update description successfully`() {
        // GIVEN
        val resource = Resource(
            id = id,
            wordType = "OBJECT"
        )
        every { resourceRepository.findById(id) } returns Optional.of(resource)
        every { resourceRepository.save(resource) } returns resource

        // WHEN
        val result: ResourceDto = resourceService.updateDescription(id, description)

        // THEN
        verify(exactly = 1) { resourceRepository.findById(id) }
        verify(exactly = 1) { resourceRepository.save(resource) }

        assertEquals(id, result.id)
        assertEquals(description, result.description)
    }

    @Test
    fun `Should throw EntityNotFoundException if it does not exist`() {
        // GIVEN
        every { resourceRepository.findById(id) } returns Optional.empty()

        // WHEN
        val exception = assertThrows<EntityNotFoundException> {
            resourceService.updateDescription(id, description)
        }

        // THEN
        assertEquals("Resource not found by id=$id", exception.message)
        verify(exactly = 1) { resourceRepository.findById(id) }
    }
}
