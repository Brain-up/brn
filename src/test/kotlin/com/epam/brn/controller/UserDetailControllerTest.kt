package com.epam.brn.controller

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.model.Gender
import com.epam.brn.service.UserAccountService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.commons.lang3.math.NumberUtils
import org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
internal class UserDetailControllerTest {

    @InjectMockKs
    lateinit var userDetailController: UserDetailController

    @MockK
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
            every { userAccountService.findUserById(userId) } returns userAccountResponse

            // WHEN
            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto =
                userDetailController.findUserById(userId).body?.data as List<UserAccountCreateRequest>

            // THEN
            verify(exactly = 1) { userAccountService.findUserById(userId) }
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountResponse)
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
        }

        @Test
        fun `should get logged in user from the current session`() {
            // GIVEN
            every { userAccountService.getUserFromTheCurrentSession() } returns userAccountResponse

            // WHEN
            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto = userDetailController.getCurrentUser().body?.data as List<UserAccountCreateRequest>

            // THEN
            verify(exactly = 1) { userAccountService.getUserFromTheCurrentSession() }
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountResponse)
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
        }

        @Test
        fun `should update avatar for current user`() {
            // GIVEN
            val avatarUrl = "xxx/www/eee"
            val userAccountResponse = UserAccountResponse(
                id = NumberUtils.LONG_ONE,
                avatar = null,
                name = "testName",
                email = "email",
                active = true,
                gender = Gender.FEMALE,
                bornYear = 2000
            )
            every { userAccountService.updateAvatarForCurrentUser(avatarUrl) } returns userAccountResponse

            // WHEN
            val response = userDetailController.updateAvatarCurrentUser(avatarUrl).body?.data as UserAccountResponse

            // THEN
            verify(exactly = 1) { userAccountService.updateAvatarForCurrentUser(avatarUrl) }
            userAccountResponse.avatar = avatarUrl
            assertEquals(userAccountResponse, response)
        }

        @Test
        fun `should update current user`() {
            // GIVEN
            val changeRequest = UserAccountChangeRequest(
                name = "testNewName",
                gender = Gender.FEMALE,
                bornYear = 2000
            )
            val userAccountResponse = UserAccountResponse(
                id = NumberUtils.LONG_ONE,
                avatar = null,
                name = "testName",
                email = "email",
                gender = Gender.FEMALE,
                active = true,
                bornYear = 2000
            )
            every { userAccountService.updateCurrentUser(changeRequest) } returns userAccountResponse

            // WHEN
            val response = userDetailController.updateCurrentUser(changeRequest).body?.data as UserAccountResponse

            // THEN
            verify(exactly = 1) { userAccountService.updateCurrentUser(changeRequest) }
            assertEquals(userAccountResponse, response)
        }

        @Test
        fun `should save headphones to user with valid type`() {
            // GIVEN
            val headphonesDto = HeadphonesDto(
                name = "test",
                type = HeadphonesType.IN_EAR_BLUETOOTH
            )
            every { userAccountService.addHeadphonesToUser(1L, headphonesDto) } returns headphonesDto

            // WHEN
            val response = userDetailController.addHeadphonesToUser(1, headphonesDto).body?.data as HeadphonesDto

            // THEN
            verify(exactly = 1) { userAccountService.addHeadphonesToUser(1L, headphonesDto) }
            assertEquals(headphonesDto, response)
        }

        @Test
        fun `should throw exception with default type of headphones`() {
            // GIVEN
            val headphonesDto = HeadphonesDto(
                name = "test",
                type = HeadphonesType.NOT_DEFINED
            )
            every { userAccountService.addHeadphonesToUser(1L, headphonesDto) } returns headphonesDto

            // WHEN
            assertFailsWith<IllegalArgumentException> {
                userDetailController.addHeadphonesToUser(1L, headphonesDto)
            }
        }

        @Test
        fun `should save headphones to current user`() {
            // GIVEN
            val headphonesDto = HeadphonesDto(
                name = "test",
                type = HeadphonesType.IN_EAR_BLUETOOTH
            )
            every { userAccountService.addHeadphonesToCurrentUser(headphonesDto) } returns headphonesDto

            // WHEN
            val response = userDetailController.addHeadphonesToCurrentUser(headphonesDto).body?.data as HeadphonesDto

            // THEN
            verify(exactly = 1) { userAccountService.addHeadphonesToCurrentUser(headphonesDto) }
            assertEquals(headphonesDto, response)
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
            every { userAccountService.getAllHeadphonesForUser(1L) } returns setOf(headphonesDto, headphonesDtoSecond)

            // WHEN
            val response = userDetailController.getAllHeadphonesForUser(1).body?.data as List<Any>

            // THEN
            verify(exactly = 1) { userAccountService.getAllHeadphonesForUser(1L) }
            assertThat(response).hasSize(2).containsExactly(headphonesDto, headphonesDtoSecond)
        }
    }
}
