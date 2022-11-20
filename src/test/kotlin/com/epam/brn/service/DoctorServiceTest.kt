package com.epam.brn.service

import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.enums.BrnGender
import com.epam.brn.model.UserAccount
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class DoctorServiceTest {

    @InjectMockKs
    private lateinit var doctorService: DoctorService

    @MockK
    private lateinit var userAccountService: UserAccountService

    @MockK
    private lateinit var roleService: RoleService

    private lateinit var admin: UserAccountResponse
    private lateinit var doctor: UserAccountResponse
    private lateinit var anotherDoctor: UserAccountResponse
    private lateinit var user1: UserAccountResponse
    private lateinit var user2: UserAccountResponse
    private lateinit var fakeDoctorUser: UserAccountResponse

    @BeforeEach
    fun setUp() {
        admin = prepareUser(0, "admin@doctor.test")
        doctor = prepareUser(1, "doctor1@doctor.test")
        anotherDoctor = prepareUser(2, "doctor2@doctor.test")
        user1 = prepareUser(3, "user1@doctor.test")
        user2 = prepareUser(4, "user2@doctor.test")
        fakeDoctorUser = prepareUser(5, "user2@doctor.test")

        every { userAccountService.findUserById(admin.id!!) } returns admin
        every { userAccountService.findUserById(doctor.id!!) } returns doctor
        every { userAccountService.findUserById(anotherDoctor.id!!) } returns anotherDoctor
        every { userAccountService.findUserById(user1.id!!) } returns user1
        every { userAccountService.findUserById(user2.id!!) } returns user2
        every { userAccountService.findUserById(fakeDoctorUser.id!!) } returns fakeDoctorUser

        every { roleService.isCurrentUserAdmin() } returns false
        every { roleService.isUserHasRole(admin, BrnRole.ADMIN) } returns true
        every { roleService.isUserHasRole(doctor, BrnRole.ADMIN) } returns false
        every { roleService.isUserHasRole(doctor, BrnRole.SPECIALIST) } returns true
        every { roleService.isUserHasRole(anotherDoctor, BrnRole.ADMIN) } returns false
        every { roleService.isUserHasRole(anotherDoctor, BrnRole.SPECIALIST) } returns true
        every { roleService.isUserHasRole(user1, BrnRole.ADMIN) } returns false
        every { roleService.isUserHasRole(user1, BrnRole.SPECIALIST) } returns false
        every { roleService.isUserHasRole(user2, BrnRole.ADMIN) } returns false
        every { roleService.isUserHasRole(user2, BrnRole.SPECIALIST) } returns false
        every { roleService.isUserHasRole(fakeDoctorUser, BrnRole.ADMIN) } returns false
        every { roleService.isUserHasRole(fakeDoctorUser, BrnRole.SPECIALIST) } returns false

        every { userAccountService.updateDoctorForPatient(any(), any()) } returns mockk()
        every { userAccountService.removeDoctorFromPatient(any()) } returns mockk()
    }

    // =================================================================================================================
    // Tests for addPatientToDoctorAsDoctor
    // =================================================================================================================
    @Test
    fun `should add doctor to user`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns doctor

        // WHEN
        doctorService.addPatientToDoctorAsDoctor(doctor.id!!, user1.id!!)

        // THEN
        verify { userAccountService.updateDoctorForPatient(user1.id!!, doctor.id!!) }
    }

    @Test
    fun `should not add doctor to user for another doctor`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns doctor

        // WHEN
        assertThrows<IllegalArgumentException> {
            doctorService.addPatientToDoctorAsDoctor(anotherDoctor.id!!, user1.id!!)
        }

        // THEN
        verify(exactly = 0) { userAccountService.updateDoctorForPatient(user1.id!!, doctor.id!!) }
    }

    @Test
    fun `should not add doctor to user since doctor is not doctor`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns fakeDoctorUser

        // WHEN
        assertThrows<IllegalArgumentException> {
            doctorService.addPatientToDoctorAsDoctor(fakeDoctorUser.id!!, user1.id!!)
        }

        // THEN
        verify(exactly = 0) { userAccountService.updateDoctorForPatient(fakeDoctorUser.id!!, user1.id!!) }
    }

    @Test
    fun `should not add doctor to user if user already has a doctor`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns doctor
        user1.doctorId = anotherDoctor.id

        // WHEN
        assertThrows<IllegalArgumentException> {
            doctorService.addPatientToDoctorAsDoctor(doctor.id!!, user1.id!!)
        }

        // THEN
        verify(exactly = 0) { userAccountService.updateDoctorForPatient(user1.id!!, doctor.id!!) }
    }

    @Test
    fun `should not add doctor to user if user is doctor`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns doctor

        // WHEN
        assertThrows<IllegalArgumentException> {
            doctorService.addPatientToDoctorAsDoctor(doctor.id!!, anotherDoctor.id!!)
        }

        // THEN
        verify(exactly = 0) { userAccountService.updateDoctorForPatient(anotherDoctor.id!!, doctor.id!!) }
    }

    // =================================================================================================================
    // Tests for removeDoctorFromPatientAsDoctor
    // =================================================================================================================
    @Test
    fun `should remove doctor from patient as doctor`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns doctor
        user1.doctorId = doctor.id

        // WHEN
        doctorService.deleteDoctorFromPatientAsDoctor(doctor.id!!, user1.id!!)

        // THEN
        verify { userAccountService.removeDoctorFromPatient(user1.id!!) }
    }

    @Test
    fun `should not remove doctor from patient as doctor if requester is not a doctor`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns fakeDoctorUser
        user1.doctorId = doctor.id

        // WHEN
        assertThrows<IllegalArgumentException> {
            doctorService.deleteDoctorFromPatientAsDoctor(fakeDoctorUser.id!!, user1.id!!)
        }

        // THEN
        verify(exactly = 0) { userAccountService.removeDoctorFromPatient(user1.id!!) }
    }

    @Test
    fun `should not remove doctor from patient as doctor if user has another doctor`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns doctor
        user1.doctorId = anotherDoctor.id

        // WHEN
        assertThrows<IllegalArgumentException> {
            doctorService.deleteDoctorFromPatientAsDoctor(doctor.id!!, user1.id!!)
        }

        // THEN
        verify(exactly = 0) { userAccountService.removeDoctorFromPatient(user1.id!!) }
    }

    @Test
    fun `should remove any doctor from any patient if current user is admin`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns admin

        // WHEN
        doctorService.deleteDoctorFromPatientAsDoctor(doctor.id!!, user1.id!!)

        // THEN
        verify { userAccountService.removeDoctorFromPatient(user1.id!!) }
    }

    // =================================================================================================================
    // Tests for removeDoctorFromPatientAsPatient
    // =================================================================================================================
    @Test
    fun `should remove doctor from patient as patient`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns user1.also { it.doctorId = doctor.id }

        // WHEN
        doctorService.deleteDoctorFromPatientAsPatient(user1.id!!)

        // THEN
        verify { userAccountService.removeDoctorFromPatient(user1.id!!) }
    }

    @Test
    fun `should remove doctor from patient as patient if current user is admin`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns admin

        // WHEN
        doctorService.deleteDoctorFromPatientAsPatient(user1.id!!)

        // THEN
        verify { userAccountService.removeDoctorFromPatient(user1.id!!) }
    }

    // =================================================================================================================
    // Tests for getPatientsForDoctor
    // =================================================================================================================
    @Test
    fun `should get patients for doctor`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns doctor
        every { userAccountService.getPatientsForDoctor(doctor.id!!) } returns listOf(user1, user2)

        // WHEN
        val patientsResult = doctorService.getPatientsForDoctor(doctor.id!!)

        // THEN
        verify { userAccountService.getPatientsForDoctor(doctor.id!!) }
        patientsResult.contains(user1) shouldBe true
        patientsResult.contains(user2) shouldBe true
    }

    @Test
    fun `should not get patients for another doctor`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns doctor

        // WHEN
        assertThrows<IllegalArgumentException> {
            doctorService.getPatientsForDoctor(anotherDoctor.id!!)
        }

        // THEN
        verify(exactly = 0) { userAccountService.getPatientsForDoctor(doctor.id!!) }
    }

    @Test
    fun `should get patients for any doctor if current user is admin`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns admin
        every { userAccountService.getPatientsForDoctor(doctor.id!!) } returns listOf(user1, user2)

        // WHEN
        val patientsResult = doctorService.getPatientsForDoctor(doctor.id!!)

        // THEN
        verify { userAccountService.getPatientsForDoctor(doctor.id!!) }
        patientsResult.contains(user1) shouldBe true
        patientsResult.contains(user2) shouldBe true
    }

    // =================================================================================================================
    // Tests for getDoctorAssignedToPatient
    // =================================================================================================================
    @Test
    fun `should get doctor assigned to patient`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns user1
        user1.doctorId = doctor.id

        // WHEN
        val doctorAssignedToPatient = doctorService.getDoctorAssignedToPatient(user1.id!!)

        // THEN
        verify { userAccountService.findUserById(doctor.id!!) }
        doctorAssignedToPatient.id shouldBe doctor.id!!
    }

    @Test
    fun `should not get doctor assigned to patient if no doctor`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns user1
        user1.doctorId = null

        // WHEN
        assertThrows<IllegalArgumentException> {
            doctorService.getDoctorAssignedToPatient(user1.id!!)
        }

        // THEN
        verify(exactly = 0) { userAccountService.findUserById(doctor.id!!) }
    }

    @Test
    fun `should not get doctor assigned to another patient`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns user1.also { it.doctorId = doctor.id }

        // WHEN
        assertThrows<IllegalArgumentException> {
            doctorService.getDoctorAssignedToPatient(user2.id!!)
        }

        // THEN
        verify(exactly = 0) { userAccountService.findUserById(doctor.id!!) }
    }

    @Test
    fun `should get doctor assigned any patient if current user is admin`() {
        // GIVEN
        every { userAccountService.getCurrentUser().toDto() } returns admin
        user1.doctorId = doctor.id

        // WHEN
        val doctorAssignedToPatient = doctorService.getDoctorAssignedToPatient(user1.id!!)

        // THEN
        verify { userAccountService.findUserById(doctor.id!!) }
        doctorAssignedToPatient.id shouldBe doctor.id!!
    }

    // =================================================================================================================

    @Test
    fun `checkUserIsNotAdmin should throw exception if user used admin ID`() {
        every { userAccountService.getCurrentUser() } returns UserAccount(
            fullName = "testUserFirstName",
            gender = BrnGender.MALE.toString(),
            bornYear = 2000,
            email = "test@test.test"
        )

        assertThrows<IllegalArgumentException> {
            doctorService.checkUserIsNotAdmin(admin, DoctorService.USING_ADMIN_ID_FOR_PATIENT_WARN)
        }
    }

    private fun prepareUser(
        id: Long?,
        email: String?,
        doctorId: Long? = null
    ): UserAccountResponse {
        return UserAccountResponse(
            id = id,
            name = email,
            email = email,
            gender = BrnGender.MALE,
            bornYear = 2000,
            active = true,
            doctorId = doctorId
        )
    }
}
