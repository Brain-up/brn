package com.epam.brn.controller

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.Response
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.model.Gender
import com.epam.brn.service.DoctorService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import com.google.firebase.auth.FirebaseAuth
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
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

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    lateinit var firebaseAuth: FirebaseAuth

    @MockK
    private lateinit var doctorService: DoctorService

    @MockK
    private lateinit var userAnalyticsService: UserAnalyticsService

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
                active = true,
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
        fun `should save headphones to current user`() {
            // GIVEN
            val headphonesDto = HeadphonesDto(
                name = "test",
                active = true,
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
            val headphonesDto = HeadphonesDto(
                name = "test",
                active = true,
                type = HeadphonesType.IN_EAR_BLUETOOTH
            )
            val headphonesDtoSecond = HeadphonesDto(
                name = "testSecond",
                active = true,
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
        val doctor = UserAccountResponse(
            id = patientId,
            name = "testName",
            email = "email",
            gender = Gender.FEMALE,
            bornYear = 2000
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
    fun `getUsers should return users with statistic when withAnalytics is true`() {
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
        (users.body as Response<*>).data shouldBe listOf(userWithAnalyticsResponse)
    }

    @Test
    fun `getUsers should return users when withAnalytics is false`() {
        // GIVEN
        val withAnalytics = false
        val role = BrnRole.USER
        val pageable = mockk<Pageable>()
        every { userAccountService.getUsers(pageable, role) } returns listOf(userAccountResponse)

        // WHEN
        val users = userDetailController.getUsers(withAnalytics, role, pageable)

        // THEN
        verify(exactly = 1) { userAccountService.getUsers(pageable, role) }
        users.statusCodeValue shouldBe HttpStatus.SC_OK
        (users.body as Response<*>).data shouldBe listOf(userAccountResponse)
    }
}
