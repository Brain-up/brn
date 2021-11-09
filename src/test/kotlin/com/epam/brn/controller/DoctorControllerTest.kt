package com.epam.brn.controller

import com.epam.brn.dto.request.AddPatientToDoctorRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.model.Gender
import com.epam.brn.service.DoctorService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DoctorControllerTest {

    @InjectMockKs
    private lateinit var doctorController: DoctorController

    @MockK
    private lateinit var doctorService: DoctorService

    @Test
    fun `should add patient to doctor`() {
        // GIVEN
        val doctorId: Long = 1
        val patientId: Long = 2
        every { doctorService.addPatientToDoctorAsDoctor(doctorId, patientId) } returns Unit

        val addPatientToDoctorRequest = AddPatientToDoctorRequest(patientId, "user")

        // WHEN
        doctorController.addPatientToDoctor(doctorId, addPatientToDoctorRequest)

        // THEN
        verify { doctorService.addPatientToDoctorAsDoctor(doctorId, patientId) }
    }

    @Test
    fun `should get all patient for doctor`() {
        // GIVEN
        val doctorId: Long = 1

        val user1 = prepareUser(id = 2, email = "user1@test.test")
        val user2 = prepareUser(id = 3, email = "user2@test.test")

        val patients = listOf(user1, user2)
        every { doctorService.getPatientsForDoctor(doctorId) } returns patients

        // WHEN
        val patientsResult = doctorController.getAllPatientForDoctor(doctorId)

        // THEN
        verify { doctorService.getPatientsForDoctor(doctorId) }
        patientsResult.body?.data?.size shouldBe patients.size
        patientsResult.body?.data?.contains(user1) shouldBe true
        patientsResult.body?.data?.contains(user2) shouldBe true
    }

    @Test
    fun `should delete patient from doctor`() {
        // GIVEN
        val doctorId: Long = 1
        val userId: Long = 2
        every { doctorService.deleteDoctorFromPatientAsDoctor(doctorId, userId) } returns Unit

        // WHEN
        doctorController.deletePatientFromDoctor(doctorId, userId)

        // THEN
        verify { doctorService.deleteDoctorFromPatientAsDoctor(doctorId, userId) }
    }

    private fun prepareUser(
        id: Long?,
        email: String?,
        authorities: MutableSet<String>? = mutableSetOf(),
        doctorId: Long? = null
    ): UserAccountResponse {
        return UserAccountResponse(
            id = id,
            name = email,
            email = email,
            gender = Gender.MALE,
            bornYear = 2000,
            active = true,
            doctorId = doctorId
        ).apply { this.authorities = authorities }
    }
}
