package com.epam.brn.controller

import com.epam.brn.config.UserDetailControllerConfig
import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.enums.BrnGender
import com.epam.brn.enums.BrnRole
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.service.DoctorService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import com.epam.brn.service.UserAnalyticsServiceV1
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.lang3.math.NumberUtils
import org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE
import org.apache.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Pageable
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class UserDetailControllerTest {
    @InjectMockKs
    lateinit var userDetailController: UserDetailController

    @RelaxedMockK
    lateinit var config: UserDetailControllerConfig

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    private lateinit var doctorService: DoctorService

    @MockK
    private lateinit var userAnalyticsService: UserAnalyticsService

    @MockK
    private lateinit var userAnalyticsServiceV1: UserAnalyticsServiceV1

    lateinit var userAccountDto: UserAccountDto

    val userId: Long = NumberUtils.LONG_ONE

    @BeforeEach
    fun initBeforeEachTest() {
        userAccountDto =
            UserAccountDto(
                id = userId,
                name = "testUserFirstName",
                email = "unittest@test.ru",
                gender = BrnGender.MALE,
                bornYear = 2000,
                active = true,
            )
    }

    @Nested
    @DisplayName("Tests for user accounts")
    inner class GetUserAccounts {
        @Test
        fun `should get user by id`() {
            // GIVEN
            every { userAccountService.findUserDtoById(userId) } returns userAccountDto

            // WHEN
            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto =
                userDetailController.findUserById(userId).body?.data as List<UserAccountCreateRequest>

            // THEN
            verify(exactly = 1) { userAccountService.findUserDtoById(userId) }
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountDto)
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
        }

        @Test
        fun `should get logged in user from the current session`() {
            // GIVEN
            every { userAccountService.getCurrentUserDto() } returns userAccountDto

            // WHEN
            @Suppress("UNCHECKED_CAST")
            val savedUserAccountDto = userDetailController.getCurrentUser().body?.data as List<UserAccountCreateRequest>

            // THEN
            verify(exactly = 1) { userAccountService.getCurrentUserDto() }
            assertThat(savedUserAccountDto[0]).isEqualTo(userAccountDto)
            assertThat(savedUserAccountDto).hasSize(INTEGER_ONE)
        }

        @Test
        fun `should update avatar for current user`() {
            // GIVEN
            val avatarUrl = "xxx/www/eee"
            val userAccountDto =
                UserAccountDto(
                    id = NumberUtils.LONG_ONE,
                    avatar = null,
                    name = "testName",
                    email = "email",
                    active = true,
                    gender = BrnGender.FEMALE,
                    bornYear = 2000,
                )
            every { userAccountService.updateAvatarForCurrentUser(avatarUrl) } returns userAccountDto

            // WHEN
            val response = userDetailController.updateAvatarCurrentUser(avatarUrl).body?.data as UserAccountDto

            // THEN
            verify(exactly = 1) { userAccountService.updateAvatarForCurrentUser(avatarUrl) }
            userAccountDto.avatar = avatarUrl
            assertEquals(userAccountDto, response)
        }

        @Test
        fun `should update current user`() {
            // GIVEN
            val changeRequest =
                UserAccountChangeRequest(
                    name = "testNewName",
                    gender = BrnGender.FEMALE,
                    bornYear = 2000,
                )
            val userAccountDto =
                UserAccountDto(
                    id = NumberUtils.LONG_ONE,
                    avatar = null,
                    name = "testName",
                    email = "email",
                    gender = BrnGender.FEMALE,
                    active = true,
                    bornYear = 2000,
                )
            every { userAccountService.updateCurrentUser(changeRequest) } returns userAccountDto

            // WHEN
            val response = userDetailController.updateCurrentUser(changeRequest).body?.data as UserAccountDto

            // THEN
            verify(exactly = 1) { userAccountService.updateCurrentUser(changeRequest) }
            assertEquals(userAccountDto, response)
        }

        @Test
        fun `should save headphones to user with valid type`() {
            // GIVEN
            val headphonesDto =
                HeadphonesDto(
                    name = "test",
                    active = true,
                    type = HeadphonesType.IN_EAR_BLUETOOTH,
                )
            every { userAccountService.addHeadphonesToUser(1L, headphonesDto) } returns headphonesDto

            // WHEN
            val response = userDetailController.addHeadphonesToUser(1, headphonesDto).body?.data as HeadphonesDto

            // THEN
            verify(exactly = 1) { userAccountService.addHeadphonesToUser(1L, headphonesDto) }
            assertEquals(headphonesDto, response)
        }

        @Test
        fun `should save headphones to current user`() {
            // GIVEN
            val headphonesDto =
                HeadphonesDto(
                    name = "test",
                    active = true,
                    type = HeadphonesType.IN_EAR_BLUETOOTH,
                )
            every { userAccountService.addHeadphonesToCurrentUser(headphonesDto) } returns headphonesDto

            // WHEN
            val response = userDetailController.addHeadphonesToCurrentUser(headphonesDto).body?.data as HeadphonesDto

            // THEN
            verify(exactly = 1) { userAccountService.addHeadphonesToCurrentUser(headphonesDto) }
            assertEquals(headphonesDto, response)
        }

        @Test
        fun `should delete headphones belongs to user`() {
            // GIVEN
            val headphonesId = 1L
            justRun { userAccountService.deleteHeadphonesForCurrentUser(headphonesId) }

            // WHEN
            val response = userDetailController.deleteHeadphonesForCurrentUser(headphonesId)

            // THEN
            verify(exactly = 1) { userAccountService.deleteHeadphonesForCurrentUser(headphonesId) }
            response.statusCode.value() shouldBe HttpStatus.SC_OK
            response.body!!.data shouldBe Unit
        }

        @Test
        fun `should return all headphones belongs to user`() {
            // GIVEN
            val headphonesDto =
                HeadphonesDto(
                    name = "test",
                    active = true,
                    type = HeadphonesType.IN_EAR_BLUETOOTH,
                )
            val headphonesDtoSecond =
                HeadphonesDto(
                    name = "testSecond",
                    active = true,
                    type = HeadphonesType.IN_EAR_NO_BLUETOOTH,
                )
            every { userAccountService.getAllHeadphonesForUser(1L) } returns setOf(headphonesDto, headphonesDtoSecond)

            // WHEN
            val response = userDetailController.getAllHeadphonesForUser(1).body?.data as List<Any>

            // THEN
            verify(exactly = 1) { userAccountService.getAllHeadphonesForUser(1L) }
            assertThat(response).hasSize(2).containsExactly(headphonesDto, headphonesDtoSecond)
        }
    }

    @Test
    internal fun `should get all headphones for user`() {
        // GIVEN
        val headphone1 = HeadphonesDto(id = 1, name = "h1", type = HeadphonesType.IN_EAR_BLUETOOTH, active = true)
        val headphone2 = HeadphonesDto(id = 2, name = "h2", type = HeadphonesType.IN_EAR_NO_BLUETOOTH, active = true)
        val headphones: Set<HeadphonesDto> = setOf(headphone1, headphone2)

        every { userAccountService.getAllHeadphonesForCurrentUser() } returns headphones

        // WHEN
        val response = userDetailController.getAllHeadphonesForUser().body?.data

        // THEN
        verify { userAccountService.getAllHeadphonesForCurrentUser() }
        response?.size shouldBe headphones.size
        response?.contains(headphone1) shouldBe true
        response?.contains(headphone2) shouldBe true
    }

    @Test
    internal fun `should get doctor assigned to patient`() {
        // GIVEN
        val patientId: Long = 1
        val doctor =
            UserAccountDto(
                id = patientId,
                name = "testName",
                email = "email",
                gender = BrnGender.FEMALE,
                bornYear = 2000,
            )
        every { doctorService.getDoctorAssignedToPatient(patientId) } returns doctor

        // WHEN
        val response = userDetailController.getDoctorAssignedToPatient(patientId).body?.data

        // THEN
        verify { doctorService.getDoctorAssignedToPatient(patientId) }
        response shouldBe doctor
    }

    @Test
    internal fun `should delete doctor from patient`() {
        // GIVEN
        val patientId: Long = 1
        every { doctorService.deleteDoctorFromPatientAsPatient(patientId) } returns Unit

        // WHEN
        userDetailController.deleteDoctorFromPatient(patientId)

        // THEN
        verify { doctorService.deleteDoctorFromPatientAsPatient(patientId) }
    }

    @Test
    fun `getUsers should return users with statistics when withAnalytics is true`() {
        // GIVEN
        val withAnalytics = true
        val role = BrnRole.USER
        val pageable = mockk<Pageable>()
        val userWithAnalyticsResponse = mockk<UserWithAnalyticsResponse>()
        every { userAnalyticsService.getUsersWithAnalytics(pageable, role) } returns listOf(userWithAnalyticsResponse)

        // WHEN
        val users = userDetailController.getUsers(withAnalytics, role, pageable)

        // THEN
        verify(exactly = 1) { userAnalyticsService.getUsersWithAnalytics(pageable, role) }
        users.statusCodeValue shouldBe HttpStatus.SC_OK
        (users.body as BrnResponse<*>).data shouldBe listOf(userWithAnalyticsResponse)
    }

    @Test
    fun `getUsers should return users with statistics when withAnalytics is true for V1 analytics service version`() {
        // GIVEN
        val withAnalytics = true
        val role = BrnRole.USER
        val pageable = mockk<Pageable>()
        val userWithAnalyticsResponse = mockk<UserWithAnalyticsResponse>()
        every { config.isUseNewAnalyticsService } returns true
        every { userAnalyticsServiceV1.getUsersWithAnalytics(pageable, role) } returns listOf(userWithAnalyticsResponse)

        // WHEN
        val users = userDetailController.getUsers(withAnalytics, role, pageable)

        // THEN
        verify(exactly = 1) { userAnalyticsServiceV1.getUsersWithAnalytics(pageable, role) }
        users.statusCodeValue shouldBe HttpStatus.SC_OK
        (users.body as BrnResponse<*>).data shouldBe listOf(userWithAnalyticsResponse)
    }

    @Test
    fun `getUsers should return users when withAnalytics is false`() {
        // GIVEN
        val withAnalytics = false
        val role = BrnRole.USER
        val pageable = mockk<Pageable>()
        every { userAccountService.getUsers(pageable, role) } returns listOf(userAccountDto)

        // WHEN
        val users = userDetailController.getUsers(withAnalytics, role, pageable)

        // THEN
        verify(exactly = 1) { userAccountService.getUsers(pageable, role) }
        users.statusCodeValue shouldBe HttpStatus.SC_OK
        (users.body as BrnResponse<*>).data shouldBe listOf(userAccountDto)
    }

    @Test
    fun `deleteAutoTestUsers should return count of deleted users`() {
        // GIVEN
        val usersCount = 2L
        every { userAccountService.deleteAutoTestUsers() } returns usersCount

        // WHEN
        val result = userDetailController.deleteAutoTestUsers()

        // THEN
        verify { userAccountService.deleteAutoTestUsers() }
        result.statusCodeValue shouldBe HttpStatus.SC_OK
        (result.body as BrnResponse<*>).data shouldBe usersCount
    }

    @Test
    fun `deleteAutoTestUserByEmail should return count of deleted users`() {
        // GIVEN
        val email = "autotest_n@170472339.1784415.com"
        val usersCount = 1L
        every { userAccountService.deleteAutoTestUserByEmail(email) } returns usersCount

        // WHEN
        val result = userDetailController.deleteAutoTestUserByEmail(email)

        // THEN
        verify { userAccountService.deleteAutoTestUserByEmail(email) }
        result.statusCodeValue shouldBe HttpStatus.SC_OK
        (result.body as BrnResponse<*>).data shouldBe usersCount
    }
}
