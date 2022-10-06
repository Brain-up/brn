package com.epam.brn.controller

import com.epam.brn.dto.ResourceDto
import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.service.ResourceService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ResourceControllerTest {

    @InjectMockKs
    lateinit var resourceController: ResourceController

    @MockK
    lateinit var resourceService: ResourceService

    @Test
    fun updateResourceDescription() {
        // GIVEN
        val id = 1L
        val description = "description"
        val request = mockk<UpdateResourceDescriptionRequest>()
        val resourceDto = mockk<ResourceDto>()
        every { request.description } returns description
        every { resourceService.updateDescription(id, description) } returns resourceDto

        // WHEN
        val updated = resourceController.updateResourceDescription(id, request)

        // THEN
        verify(exactly = 1) { resourceService.updateDescription(id, description) }
        updated.statusCodeValue shouldBe HttpStatus.SC_OK
        updated.body!!.data shouldBe resourceDto
    }
}
