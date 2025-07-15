package com.epam.brn.auth

import com.epam.brn.dto.UserAccountDto
import com.epam.brn.enums.BrnGender
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Role
import com.epam.brn.repo.RoleRepository
import com.epam.brn.service.impl.RoleServiceImpl
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
@DisplayName("RoleServiceImplTest test using MockK")
internal class RoleServiceImplTest {
    @InjectMockKs
    private lateinit var roleServiceImpl: RoleServiceImpl

    @MockK
    private lateinit var roleRepository: RoleRepository

    @Test
    fun `findById should get role with roleId`() {
        // GIVEN
        val roleIdLong = 1L
        val role =
            Role(
                id = 1L,
                name = "FirstName",
            )
        every { roleRepository.findById(roleIdLong) } returns (Optional.of(role))

        // WHEN
        val roleById = roleServiceImpl.findById(roleIdLong)

        // THEN
        verify(exactly = 1) { roleRepository.findById(roleIdLong) }
        assertEquals(roleById, role)
    }

    @Test
    fun `findByName should return role`() {
        // GIVEN
        val roleName = "Name"
        val role =
            Role(
                id = 1L,
                name = "FirstName",
            )
        every { roleRepository.findByName(roleName) } returns (role)

        // WHEN
        val foundRole = roleServiceImpl.findByName(roleName)

        // THEN
        verify(exactly = 1) { roleRepository.findByName(roleName) }
        assertEquals(foundRole, role)
    }

    @Test
    fun `should throw error when role by Id is not found`() {
        // GIVEN
        val roleId = 1L
        every { roleRepository.findById(roleId) } returns Optional.empty()

        // WHEN
        assertFailsWith<EntityNotFoundException> {
            roleServiceImpl.findById(roleId)
        }
    }

    @Test
    fun `should throw error when role by Name is not found`() {
        // GIVEN
        val roleName = ""
        every { roleRepository.findByName(roleName) } returns null

        // WHEN
        assertFailsWith<EntityNotFoundException> {
            roleServiceImpl.findByName(roleName)
        }
    }

    @Test
    fun `should save role`() {
        // GIVEN
        val role =
            Role(
                id = 1L,
                name = "FirstName",
            )
        every { roleRepository.save(role) } returns (role)
        // WHEN
        val resultSaving = roleServiceImpl.save(role)
        // THEN
        verify(exactly = 1) { roleRepository.save(role) }
        assertEquals(role, resultSaving)
    }

    @Test
    fun `should find all roles`() {
        // GIVEN
        val roleOne =
            Role(
                id = 1L,
                name = "FirstName",
            )
        val roleList = listOf(roleOne)
        every { roleRepository.findAll() } returns (roleList)
        // WHEN
        val allRoles = roleServiceImpl.findAll()
        // THEN
        verify(exactly = 1) { roleRepository.findAll() }
        assertEquals(1, allRoles.size)
    }

    @Test
    fun `should check users roles`() {
        // GIVEN
        val user = UserAccountDto(email = "email", bornYear = 1111, name = "name", gender = BrnGender.FEMALE)
        user.roles = mutableSetOf("USER")
        // WHEN
        val result = roleServiceImpl.isUserHasRole(user, "USER")
        // THEN
        result shouldBe true
    }

    @Test
    fun `should check users has not role`() {
        // GIVEN
        val user = UserAccountDto(email = "email", bornYear = 1111, name = "name", gender = BrnGender.FEMALE)
        user.roles = mutableSetOf("USER")
        // WHEN
        val result = roleServiceImpl.isUserHasRole(user, "ADMIN")
        // THEN
        result shouldBe false
    }
}
