package com.epam.brn.service

import com.epam.brn.dto.ResourceDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Resource
import com.epam.brn.repo.ResourceRepository
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
internal class ResourceServiceTest {
    @InjectMocks
    lateinit var resourceService: ResourceService

    @Mock
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
        `when`(resourceRepository.findById(id)).thenReturn(Optional.of(resource))
        `when`(resourceRepository.save(resource)).thenReturn(resource)

        // WHEN
        val result: ResourceDto = resourceService.updateDescription(id, description)

        // THEN
        assertEquals(id, result.id)
        assertEquals(description, result.description)
        verify(resourceRepository).findById(id)
        verify(resourceRepository).save(resource)
    }

    @Test
    fun `Should throw EntityNotFoundException if it does not exist`() {
        // GIVEN
        `when`(resourceRepository.findById(id)).thenReturn(Optional.empty())

        // WHEN
        val exception = assertThrows<EntityNotFoundException> {
            resourceService.updateDescription(id, description)
        }

        // THEN
        assertEquals("Resource not found by id=$id", exception.message)
        verify(resourceRepository).findById(id)
        verifyNoMoreInteractions(resourceRepository)
    }
}
