package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.request.AddPatientToDoctorRequest
import com.epam.brn.service.DoctorService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
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

@RestController
@RequestMapping("/doctors")
@Api(value = "/doctors", description = "Contains actions for doctor")
class DoctorController(private val doctorService: DoctorService) {

    @PostMapping("/{doctorId}/patients")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Add patient to doctor")
    fun addPatientToDoctor(
        @PathVariable doctorId: Long,
        @Validated @RequestBody addPatientToDoctorRequest: AddPatientToDoctorRequest
    ) = doctorService.addPatientToDoctorAsDoctor(doctorId, addPatientToDoctorRequest.id)

    @GetMapping("/{doctorId}/patients")
    @ApiOperation("Get all patients for doctor")
    fun getAllPatientForDoctor(@PathVariable doctorId: Long): ResponseEntity<BaseResponseDto> =
        ResponseEntity.ok(BaseResponseDto(data = doctorService.getPatientsForDoctor(doctorId)))

    @DeleteMapping("/{doctorId}/patients/{patientId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Delete patient from doctor")
    fun deletePatientFromDoctor(@PathVariable doctorId: Long, @PathVariable patientId: Long) =
        doctorService.deleteDoctorFromPatientAsDoctor(doctorId, patientId)
}
