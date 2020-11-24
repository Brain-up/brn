package com.epam.brn.controller

import com.epam.brn.dto.request.UserAccountRequest
import com.epam.brn.dto.response.UserAccountResponse
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

    lateinit var userAccountResponse: UserAccountResponse

    val userId: Long = NumberUtils.LONG_ONE

    @BeforeEach
    fun initBeforeEachTest() {
        userAccountResponse = UserAccountResponse(
            id = userId,
            name = "testUserFirstName",
            email = "unittest@test.ru",
            gender = Gender.MALE,
            bornYear = 2000,
            active = true
        )
    }

    @Nested
    @DisplayName("Tests for user accounts")
    inner class GetUserAccounts {
        @Test
        fun `should get user by id`() {
            // GIVEN
            Mockito.`when`(userAccountService.findUserById(userId)).thenReturn(userAccountResponse)
            // WHEN

            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto = userDetailController.findUserById(userId).body?.data as List<UserAccountRequest>
            // THEN
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountResponse)
            verify(userAccountService).findUserById(userId)
        }

        @Test
        fun `should get logged in user from the current session`() {
            // GIVEN
            Mockito.`when`(userAccountService.getUserFromTheCurrentSession()).thenReturn(userAccountResponse)
            // WHEN
            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto = userDetailController.getCurrentUser().body?.data as List<UserAccountRequest>
            // THEN
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountResponse)
            verify(userAccountService).getUserFromTheCurrentSession()
        }
    }
}
