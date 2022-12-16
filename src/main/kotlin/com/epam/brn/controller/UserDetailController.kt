package com.epam.brn.controller

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.DoctorService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Contains actions over user details and accounts")
@RolesAllowed(BrnRole.USER)
class UserDetailController(
    private val userAccountService: UserAccountService,
    private val doctorService: DoctorService,
    private val userAnalyticsService: UserAnalyticsService
) {
    @GetMapping
    @Operation(summary = "Get all users with/without analytic data")
    @RolesAllowed(BrnRole.ADMIN)
    fun getUsers(
        @RequestParam("withAnalytics", defaultValue = "false") withAnalytics: Boolean,
        @RequestParam("role", defaultValue = "USER") role: String,
        @PageableDefault pageable: Pageable,
    ): ResponseEntity<Any> {
        val users = if (withAnalytics) userAnalyticsService.getUsersWithAnalytics(pageable, role)
        else userAccountService.getUsers(pageable, role)
        return ResponseEntity.ok().body(BrnResponse(data = users))
    }

    @GetMapping(value = ["/{userId}"])
    @Operation(summary = "Get user by id")
    @RolesAllowed(BrnRole.ADMIN)
    fun findUserById(@PathVariable("userId") id: Long): ResponseEntity<BrnResponse<List<UserAccountResponse>>> {
        return ResponseEntity.ok()
            .body(BrnResponse(data = listOf(userAccountService.findUserById(id))))
    }

    @GetMapping(value = ["/current"])
    @Operation(summary = "Get current logged in user")
    fun getCurrentUser() = ResponseEntity.ok()
        .body(BrnResponse(data = listOf(userAccountService.getUserFromTheCurrentSession())))

    @PatchMapping(value = ["/current"])
    @Operation(summary = "Update current logged in user")
    fun updateCurrentUser(@Validated @RequestBody userAccountChangeRequest: UserAccountChangeRequest) =
        ResponseEntity.ok()
            .body(BrnResponse(data = userAccountService.updateCurrentUser(userAccountChangeRequest)))

    @PutMapping(value = ["/current/avatar"])
    @Operation(summary = "Update avatar current user")
    fun updateAvatarCurrentUser(
        @RequestParam("avatar", required = true) avatar: String
    ) = ResponseEntity.ok()
        .body(BrnResponse(data = userAccountService.updateAvatarForCurrentUser(avatar)))

    @PostMapping(value = ["/{userId}/headphones"])
    @Operation(summary = "Add headphones to the user")
    @RolesAllowed(BrnRole.ADMIN)
    fun addHeadphonesToUser(
        @PathVariable("userId", required = true) userId: Long,
        @Validated @RequestBody headphones: HeadphonesDto
    ) = ResponseEntity.status(HttpStatus.CREATED)
        .body(BrnResponse(data = userAccountService.addHeadphonesToUser(userId, headphones)))

    @PostMapping(value = ["/current/headphones"])
    @Operation(summary = "Add headphones to current user")
    fun addHeadphonesToCurrentUser(@Validated @RequestBody headphones: HeadphonesDto) =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(BrnResponse(data = userAccountService.addHeadphonesToCurrentUser(headphones)))

    @DeleteMapping(value = ["/current/headphones/{headphonesId}"])
    @Operation(summary = "Delete headphone by id")
    fun deleteHeadphonesForCurrentUser(
        @PathVariable(value = "headphonesId") headphonesId: Long
    ): ResponseEntity<BrnResponse<Any>> {
        userAccountService.deleteHeadphonesForCurrentUser(headphonesId)
        return ResponseEntity.ok(BrnResponse(data = Unit))
    }

    @GetMapping(value = ["/{userId}/headphones"])
    @Operation(summary = "Get all user's headphones")
    @RolesAllowed(BrnRole.ADMIN)
    fun getAllHeadphonesForUser(
        @PathVariable("userId", required = true) userId: Long
    ) = ResponseEntity
        .ok()
        .body(BrnResponse(data = userAccountService.getAllHeadphonesForUser(userId).toList()))

    @GetMapping(value = ["/current/headphones"])
    @Operation(summary = "Get all headphones for current user")
    fun getAllHeadphonesForUser() = ResponseEntity
        .ok()
        .body(BrnResponse(data = userAccountService.getAllHeadphonesForCurrentUser().toList()))

    @GetMapping("/current/{patientId}/doctor")
    @Operation(summary = "Get patient's doctor")
    fun getDoctorAssignedToPatient(@PathVariable patientId: Long) =
        ResponseEntity.ok()
            .body(BrnResponse(data = doctorService.getDoctorAssignedToPatient(patientId)))

    @DeleteMapping("/current/{patientId}/doctor")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete doctor from patient")
    fun deleteDoctorFromPatient(@PathVariable patientId: Long) =
        doctorService.deleteDoctorFromPatientAsPatient(patientId)
}
