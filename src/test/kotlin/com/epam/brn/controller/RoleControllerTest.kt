package com.epam.brn.controller

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.response.AuthorityResponse
import com.epam.brn.model.Authority
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
    lateinit var authorityService: AuthorityService

    @Test
    fun `getRoles should return http status 200`() {
        // GIVEN
        val authority = mockk<Authority>()
        val authorityResponse = mockk<AuthorityResponse>()
        every { authorityService.findAll() } returns listOf(authority)
        every { authority.toDto() } returns authorityResponse

        // WHEN
        val authorities = roleController.getRoles()

        // THEN
        authorities.statusCodeValue shouldBe HttpStatus.SC_OK
        authorities.body!!.data.size shouldBe 1
    }
}
