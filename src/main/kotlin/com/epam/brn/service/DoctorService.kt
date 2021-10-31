package com.epam.brn.service

import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.enums.Role
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class DoctorService(private val userAccountService: UserAccountService) {
    private val log = logger()

    fun addPatientToDoctorAsDoctor(doctorId: Long, patientId: Long) {
        val currentUser = userAccountService.getCurrentUser().toDto()
        val patient = userAccountService.findUserById(patientId)
        val doctor = userAccountService.findUserById(doctorId)

        checkUserIsNotAdmin(patient, USING_ADMIN_ID_FOR_PATIENT_WARN)
        checkUserIsNotAdmin(doctor, USING_ADMIN_ID_FOR_DOCTOR_WARN)

        when {
            !isAdmin(currentUser) && currentUser.id != doctorId -> {
                throw IllegalArgumentException(CHANGE_PERMISSION_WARN)
            }
            !isDoctor(doctor) -> {
                throw IllegalArgumentException("You are trying to assign patient to user that is not a doctor")
            }
            isDoctor(patient) -> {
                throw IllegalArgumentException("The patient you are trying to add is actually a doctor")
            }
            patient.doctorId != null -> {
                throw IllegalArgumentException(
                    """The patient already has a doctor. You cannot replace another doctor by yourself. 
                    |Please contact the patient (or admin, or another doctor) 
                    |to delete the current doctor of the patient."""
                        .trimMargin().replace("\n", "")
                )
            }
        }
        userAccountService.updateDoctorForPatient(patientId, doctorId)
    }

    fun deleteDoctorFromPatientAsDoctor(doctorId: Long, patientId: Long) {
        val currentUser = userAccountService.getCurrentUser().toDto()
        val patient = userAccountService.findUserById(patientId)

        checkUserIsNotAdmin(patient, USING_ADMIN_ID_FOR_PATIENT_WARN)

        when {
            !isAdmin(currentUser) && currentUser.id != doctorId -> {
                throw IllegalArgumentException(CHANGE_PERMISSION_WARN)
            }
            !isAdmin(currentUser) && patient.doctorId != doctorId -> {
                throw IllegalArgumentException(CHANGE_PERMISSION_WARN)
            }
        }

        userAccountService.removeDoctorFromPatient(patientId)
    }

    fun deleteDoctorFromPatientAsPatient(patientId: Long) {
        val currentUser = userAccountService.getCurrentUser().toDto()
        val patient = userAccountService.findUserById(patientId)

        checkUserIsNotAdmin(patient, USING_ADMIN_ID_FOR_PATIENT_WARN)

        if (!isAdmin(currentUser) && currentUser.id != patientId) {
            throw IllegalArgumentException("It is forbidden to remove a doctor from another patient")
        }
        userAccountService.removeDoctorFromPatient(patientId)
    }

    fun getPatientsForDoctor(doctorId: Long): List<UserAccountResponse> {
        val currentUser = userAccountService.getCurrentUser().toDto()
        if (!isAdmin(currentUser) && currentUser.id != doctorId)
            throw IllegalArgumentException("It is forbidden to get patients of another doctor")
        return userAccountService.getPatientsForDoctor(doctorId)
    }

    fun getDoctorAssignedToPatient(patientId: Long): UserAccountResponse {
        val patient = userAccountService.findUserById(patientId)
        val currentUser = userAccountService.getCurrentUser().toDto()
        return when {
            !isAdmin(currentUser) && currentUser.id != patientId -> {
                throw IllegalArgumentException("It is forbidden to get a doctor from another patient")
            }
            patient.doctorId == null -> throw IllegalArgumentException("No doctor found")
            else -> userAccountService.findUserById(patient.doctorId!!)
        }
    }

    fun checkUserIsNotAdmin(user: UserAccountResponse, message: String) {
        if (isAdmin(user)) {
            val currentUser = userAccountService.getCurrentUser()
            log.error("Current user '${currentUser.id}' is trying to use admin id '${user.id}'. $message")
            // We should not disclose that the user used the admin ID. So I used such strange error message
            throw IllegalArgumentException("Unexpected error. Please try again or contact admin.")
        }
    }

    fun isDoctor(user: UserAccountResponse) = userHasAuthority(user, Role.ROLE_DOCTOR.name)
    fun isAdmin(user: UserAccountResponse) = userHasAuthority(user, Role.ROLE_ADMIN.name)
    fun userHasAuthority(user: UserAccountResponse, role: String) = user.authorities?.contains(role) ?: false

    companion object {
        const val USING_ADMIN_ID_FOR_PATIENT_WARN = "Using admin ID for patient"
        const val USING_ADMIN_ID_FOR_DOCTOR_WARN = "Using admin ID for doctor"
        const val CHANGE_PERMISSION_WARN = "It is forbidden to change data for another doctor."
    }
}
