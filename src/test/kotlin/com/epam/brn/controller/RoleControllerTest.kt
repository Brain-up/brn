package com.epam.brn.controller

import com.epam.brn.service.RoleService
import com.epam.brn.dto.response.RoleResponse
import com.epam.brn.model.Role
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class RoleControllerTest {

    @InjectMockKs
    lateinit var roleController: RoleController

    @MockK
    lateinit var roleService: RoleService

    @Test
    fun `getRoles should return http status 200`() {
        // GIVEN
        val role = mockk<Role>()
        val roleResponse = mockk<RoleResponse>()
        every { roleService.findAll() } returns listOf(role)
        every { role.toDto() } returns roleResponse

        // WHEN
        val roles = roleController.getRoles()

        // THEN
        roles.statusCodeValue shouldBe HttpStatus.SC_OK
        roles.body!!.data.size shouldBe 1
    }
}
