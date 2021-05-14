package com.epam.brn.controller

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.model.Gender
import com.epam.brn.service.UserAccountService
import com.nhaarman.mockito_kotlin.times
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
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

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
            `when`(userAccountService.findUserById(userId)).thenReturn(userAccountDto)
            // WHEN

            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto =
                userDetailController.findUserById(userId).body?.data as List<UserAccountDto>
            // THEN
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountDto)
            verify(userAccountService).findUserById(userId)
        }

        @Test
        fun `should get logged in user from the current session`() {
            // GIVEN
            `when`(userAccountService.getUserFromTheCurrentSession()).thenReturn(userAccountDto)
            // WHEN
            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto = userDetailController.getCurrentUser().body?.data as List<UserAccountDto>
            // THEN
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountDto)
            verify(userAccountService).getUserFromTheCurrentSession()
        }

        @Test
        fun `should update avatar for current user`() {
            // GIVEN
            val avatarUrl = "xxx/www/eee"
            val userAccountDto = UserAccountDto(
                id = NumberUtils.LONG_ONE,
                avatar = null,
                name = "testName",
                email = "email",
                active = true,
                gender = Gender.FEMALE,
                bornYear = 2000
            )
            `when`(userAccountService.updateAvatarForCurrentUser(avatarUrl)).thenReturn(userAccountDto)
            // WHEN
            val response = userDetailController.updateAvatarCurrentUser(avatarUrl).body?.data as UserAccountDto
            // THEN
            userAccountDto.avatar = avatarUrl
            assertEquals(userAccountDto, response)
            verify(userAccountService).updateAvatarForCurrentUser(avatarUrl)
        }

        @Test
        fun `should update current user`() {
            // GIVEN
            val changeRequest = UserAccountChangeRequest(
                name = "testNewName",
                gender = Gender.FEMALE,
                bornYear = 2000
            )
            val userAccountDto = UserAccountDto(
                id = NumberUtils.LONG_ONE,
                avatar = null,
                name = "testName",
                email = "email",
                gender = Gender.FEMALE,
                active = true,
                bornYear = 2000
            )
            `when`(userAccountService.updateCurrentUser(changeRequest)).thenReturn(userAccountDto)
            // WHEN
            val response = userDetailController.updateCurrentUser(changeRequest).body?.data as UserAccountDto
            // THEN
            assertEquals(userAccountDto, response)
            verify(userAccountService).updateCurrentUser(changeRequest)
        }

        @Test
        fun `should save headphones to user`() {
            // GIVEN
            val headphonesDto = HeadphonesDto(
                name = "test",
                type = HeadphonesType.IN_EAR_BLUETOOTH
            )
            `when`(userAccountService.addHeadphonesToUser(1L, headphonesDto)).thenReturn(headphonesDto)
            // WHEN
            val response = userDetailController.addHeadphonesToUser(1, headphonesDto).body?.data as HeadphonesDto
            // THEN
            assertEquals(headphonesDto, response)
            verify(userAccountService).addHeadphonesToUser(1L, headphonesDto)
        }

        @Test
        fun `should save headphones to current user`() {
            // GIVEN
            val headphonesDto = HeadphonesDto(
                name = "test",
                type = HeadphonesType.IN_EAR_BLUETOOTH
            )
            `when`(userAccountService.addHeadphonesToCurrentUser(headphonesDto)).thenReturn(headphonesDto)
            // WHEN
            val response = userDetailController.addHeadphonesToCurrentUser(headphonesDto).body?.data as HeadphonesDto
            // THEN
            assertEquals(headphonesDto, response)
            verify(userAccountService).addHeadphonesToCurrentUser(headphonesDto)
        }

        @Test
        fun `should return all headphones belongs to user`() {
            // GIVEN
            val headphonesDto = HeadphonesDto(
                name = "test",
                type = HeadphonesType.IN_EAR_BLUETOOTH
            )
            val headphonesDtoSecond = HeadphonesDto(
                name = "testSecond",
                type = HeadphonesType.IN_EAR_NO_BLUETOOTH
            )
            `when`(userAccountService.getAllHeadphonesForUser(1L)).thenReturn(
                setOf(
                    headphonesDto,
                    headphonesDtoSecond
                )
            )
            // WHEN
            val response = userDetailController.getAllHeadphonesForUser(1).body?.data as List<Any>
            // THEN
            assertThat(response).hasSize(2).containsExactly(headphonesDto, headphonesDtoSecond)
            verify(userAccountService, times(1)).getAllHeadphonesForUser(1L)
        }
    }
}
