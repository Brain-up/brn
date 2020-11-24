package com.epam.brn.controller

import com.epam.brn.dto.UserAccountDto
import com.epam.brn.model.Gender
import com.epam.brn.service.UserAccountService
import com.nhaarman.mockito_kotlin.verify
import org.apache.commons.lang3.math.NumberUtils
import org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class UserDetailControllerTest {

    @InjectMocks
    lateinit var userDetailController: UserDetailController

    @Mock
    lateinit var userAccountService: UserAccountService

    lateinit var userAccountDto: UserAccountDto

    val userId: Long = NumberUtils.LONG_ONE

    @BeforeEach
    fun initBeforeEachTest() {
        userAccountDto = UserAccountDto(
            id = userId,
            name = "testUserFirstName",
            email = "unittest@test.ru",
            active = true,
            gender = Gender.MALE,
            bornYear = 2000,
            password = "pwd"
        )
    }

    @Nested
    @DisplayName("Tests for user accounts")
    inner class GetUserAccounts {
        @Test
        fun `should get user by id`() {
            // GIVEN
            Mockito.`when`(userAccountService.findUserById(userId)).thenReturn(userAccountDto)
            // WHEN

            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto = userDetailController.findUserById(userId).body?.data as List<UserAccountDto>
            // THEN
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountDto)
            verify(userAccountService).findUserById(userId)
        }

        @Test
        fun `should get logged in user from the current session`() {
            // GIVEN
            Mockito.`when`(userAccountService.getUserFromTheCurrentSession()).thenReturn(userAccountDto)
            // WHEN
            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto = userDetailController.getCurrentUser().body?.data as List<UserAccountDto>
            // THEN
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountDto)
            verify(userAccountService).getUserFromTheCurrentSession()
        }
    }
}
