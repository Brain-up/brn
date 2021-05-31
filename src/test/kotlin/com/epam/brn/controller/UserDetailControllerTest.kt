package com.epam.brn.controller

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountDto
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

@ExtendWith(MockKExtension::class)
internal class UserDetailControllerTest {

    @InjectMockKs
    lateinit var userDetailController: UserDetailController

    @MockK
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
            every { userAccountService.findUserById(userId) } returns userAccountDto

            // WHEN
            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto =
                userDetailController.findUserById(userId).body?.data as List<UserAccountCreateRequest>

            // THEN
            verify { userAccountService.findUserById(userId) }
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountDto)
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
        }

        @Test
        fun `should get logged in user from the current session`() {
            // GIVEN
            every { userAccountService.getUserFromTheCurrentSession() } returns userAccountDto

            // WHEN
            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto = userDetailController.getCurrentUser().body?.data as List<UserAccountCreateRequest>

            // THEN
            verify { userAccountService.getUserFromTheCurrentSession() }
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountDto)
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
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
            every { userAccountService.updateAvatarForCurrentUser(avatarUrl) } returns userAccountDto

            // WHEN
            val response = userDetailController.updateAvatarCurrentUser(avatarUrl).body?.data as UserAccountDto

            // THEN
            verify { userAccountService.updateAvatarForCurrentUser(avatarUrl) }
            userAccountDto.avatar = avatarUrl
            assertEquals(userAccountDto, response)
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
            every { userAccountService.updateCurrentUser(changeRequest) } returns userAccountDto

            // WHEN
            val response = userDetailController.updateCurrentUser(changeRequest).body?.data as UserAccountDto

            // THEN
            verify { userAccountService.updateCurrentUser(changeRequest) }
            assertEquals(userAccountDto, response)
        }

        @Test
        fun `should save headphones to user`() {
            // GIVEN
            val headphonesDto = HeadphonesDto(
                name = "test",
                type = HeadphonesType.IN_EAR_BLUETOOTH
            )
            every { userAccountService.addHeadphonesToUser(1L, headphonesDto) } returns headphonesDto

            // WHEN
            val response = userDetailController.addHeadphonesToUser(1, headphonesDto).body?.data as HeadphonesDto

            // THEN
            verify { userAccountService.addHeadphonesToUser(1L, headphonesDto) }
            assertEquals(headphonesDto, response)
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
            verify { userAccountService.addHeadphonesToCurrentUser(headphonesDto) }
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
