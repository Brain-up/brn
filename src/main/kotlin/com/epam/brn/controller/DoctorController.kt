package com.epam.brn.controller

import com.epam.brn.dto.request.AddPatientToDoctorRequest
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.DoctorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/doctors")
@Tag(name = "Doctors", description = "Contains actions for doctor")
@RolesAllowed(BrnRole.SPECIALIST)
class DoctorController(private val doctorService: DoctorService) {

    @PostMapping("/{doctorId}/patients")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Add patient to doctor")
    fun addPatientToDoctor(
        @PathVariable doctorId: Long,
        @Validated @RequestBody addPatientToDoctorRequest: AddPatientToDoctorRequest
    ) = doctorService.addPatientToDoctorAsDoctor(doctorId, addPatientToDoctorRequest.id)

    @GetMapping("/{doctorId}/patients")
    @Operation(summary = "Get all patients for doctor")
    fun getAllPatientForDoctor(@PathVariable doctorId: Long): ResponseEntity<BrnResponse<List<UserAccountResponse>>> =
        ResponseEntity.ok(BrnResponse(data = doctorService.getPatientsForDoctor(doctorId)))

    @DeleteMapping("/{doctorId}/patients/{patientId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete patient from doctor")
    fun deletePatientFromDoctor(@PathVariable doctorId: Long, @PathVariable patientId: Long) =
        doctorService.deleteDoctorFromPatientAsDoctor(doctorId, patientId)
}
