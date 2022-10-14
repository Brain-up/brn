package com.epam.brn.auth

import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Role
import com.epam.brn.repo.RoleRepository
import com.epam.brn.service.impl.RoleServiceImpl
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
@DisplayName("AuthorityServiceImplTest test using MockK")
internal class RoleServiceImplTest {

    @InjectMockKs
    private lateinit var authorityServiceImpl: RoleServiceImpl

    @MockK
    private lateinit var roleRepository: RoleRepository

    @Test
    fun `findAuthorityById should get authority with authorityId`() {
        // GIVEN
        val authorityIdLong = 1L
        val role = Role(
            id = 1L,
            name = "FirstName"
        )
        every { roleRepository.findAuthoritiesById(authorityIdLong) } returns (role)

        // WHEN
        val authorityById = authorityServiceImpl.findById(authorityIdLong)

        // THEN
        verify(exactly = 1) { roleRepository.findAuthoritiesById(authorityIdLong) }
        assertEquals(authorityById, role)
    }

    @Test
    fun `findAuthorityByAuthorityName should return authority`() {
        // GIVEN
        val authorityName = "Name"
        val role = Role(
            id = 1L,
            name = "FirstName"
        )
        every { roleRepository.findByName(authorityName) } returns (role)

        // WHEN
        val findAuthorityByAuthorityName = authorityServiceImpl.findByName(authorityName)

        // THEN
        verify(exactly = 1) { roleRepository.findByName(authorityName) }
        assertEquals(findAuthorityByAuthorityName, role)
    }

    @Test
    fun `should throw error when authority by Id is not found`() {
        // GIVEN
        val authorityId = 1L
        every { roleRepository.findAuthoritiesById(authorityId) } returns null

        // WHEN
        assertFailsWith<EntityNotFoundException> {
            authorityServiceImpl.findById(authorityId)
        }
    }

    @Test
    fun `should throw error when authority by Name is not found`() {
        // GIVEN
        val authorityName = ""
        every { roleRepository.findByName(authorityName) } returns null

        // WHEN
        assertFailsWith<EntityNotFoundException> {
            authorityServiceImpl.findByName(authorityName)
        }
    }

    @Test
    fun `should save authority`() {
        // GIVEN
        val role = Role(
            id = 1L,
            name = "FirstName"
        )
        every { roleRepository.save(role) } returns (role)
        // WHEN
        val resultSaving = authorityServiceImpl.save(role)
        // THEN
        verify(exactly = 1) { roleRepository.save(role) }
        assertEquals(role, resultSaving)
    }

    @Test
    fun `should find all authorities`() {
        // GIVEN
        val roleOne = Role(
            id = 1L,
            name = "FirstName"
        )
        val authorityList = listOf(roleOne)
        every { roleRepository.findAll() } returns (authorityList)
        // WHEN
        val allAuthorities = authorityServiceImpl.findAll()
        // THEN
        verify(exactly = 1) { roleRepository.findAll() }
        assertEquals(1, allAuthorities.size)
    }
}
