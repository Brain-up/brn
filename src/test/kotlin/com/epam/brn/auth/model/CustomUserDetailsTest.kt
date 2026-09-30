package com.epam.brn.auth.model

import com.epam.brn.model.Role
import com.epam.brn.model.UserAccount
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class CustomUserDetailsTest {
    @Test
    fun `should map a single role to a single ROLE_ prefixed authority`() {
        // GIVEN
        val userAccount = userAccount(active = true, roleNames = setOf("USER"))

        // WHEN
        val authorities = CustomUserDetails(userAccount).authorities.map { it.authority }

        // THEN
        authorities shouldContainExactly listOf("ROLE_USER")
    }

    @Test
    fun `should map every role to its ROLE_ prefixed authority`() {
        // GIVEN
        val userAccount = userAccount(active = true, roleNames = setOf("ADMIN", "USER", "SPECIALIST"))

        // WHEN
        val authorities = CustomUserDetails(userAccount).authorities.map { it.authority }

        // THEN
        authorities shouldContainExactlyInAnyOrder listOf("ROLE_ADMIN", "ROLE_USER", "ROLE_SPECIALIST")
    }

    @Test
    fun `should be enabled when the account is active`() {
        // GIVEN
        val userAccount = userAccount(active = true, roleNames = setOf("USER"))

        // THEN
        CustomUserDetails(userAccount).isEnabled shouldBe true
    }

    @Test
    fun `should not be enabled when the account is inactive`() {
        // GIVEN
        val userAccount = userAccount(active = false, roleNames = setOf("USER"))

        // THEN
        CustomUserDetails(userAccount).isEnabled shouldBe false
    }

    @Test
    fun `should expose email as username and no password`() {
        // GIVEN
        val userAccount = userAccount(active = true, roleNames = setOf("USER"))

        // WHEN
        val userDetails = CustomUserDetails(userAccount)

        // THEN
        userDetails.username shouldBe "user@test.com"
        userDetails.password shouldBe null
    }

    private fun userAccount(
        active: Boolean,
        roleNames: Set<String>,
    ): UserAccount = UserAccount(
        email = "user@test.com",
        fullName = "Test User",
        active = active,
    ).apply {
        roleSet = roleNames.mapTo(mutableSetOf()) { Role(name = it) }
    }
}
