package com.epam.brn.auth

import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.repo.AuthorityRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
@DisplayName("AuthorityServiceImplTest test using mockito")

internal class AuthorityServiceImplTest {

    @InjectMockKs
    private lateinit var authorityServiceImpl: AuthorityServiceImpl

    @MockK
    private lateinit var authorityRepository: AuthorityRepository

    @Test
    fun `findAuthorityById should get authority with authorityId`() {

        // GIVEN
        val authorityIdLong = 1L
        val authority = Authority(
            id = 1L,
            authorityName = "FirstName"
        )
        every { authorityRepository.findAuthoritiesById(authorityIdLong) } returns (authority)

        // WHEN
        val authorityById = authorityServiceImpl.findAuthorityById(authorityIdLong)

        // THEN

        verify(exactly = 1) { authorityRepository.findAuthoritiesById(authorityIdLong) }
        assertEquals(authorityById, authority)
    }

    @Test
    fun `findAuthorityByAuthorityName should return authority`() {

        // GIVEN
        val authorityName = "Name"
        val authority = Authority(
            id = 1L,
            authorityName = "FirstName"
        )
        every { authorityRepository.findAuthorityByAuthorityName(authorityName) } returns (authority)

        // WHEN
        val findAuthorityByAuthorityName = authorityServiceImpl.findAuthorityByAuthorityName(authorityName)

        // THEN
        verify(exactly = 1) { authorityRepository.findAuthorityByAuthorityName(authorityName) }
        assertEquals(findAuthorityByAuthorityName, authority)
    }

    @Test
    fun `should throw error when authority by Id is not found`() {

        // GIVEN
        val authorityId = 1L
        every { authorityRepository.findAuthoritiesById(authorityId) } throws EntityNotFoundException("Authority is not found")

        // WHEN
        assertFailsWith<EntityNotFoundException> {
            authorityRepository.findAuthoritiesById(authorityId)
        }
    }

    @Test
    fun `should throw error when authority by Name is not found`() {

        // GIVEN
        val authorityName = "Name"
        every { authorityRepository.findAuthorityByAuthorityName(authorityName) } throws EntityNotFoundException("Authority is not found")

        // WHEN
        assertFailsWith<EntityNotFoundException> {
            authorityRepository.findAuthorityByAuthorityName(authorityName)
        }
    }
}
