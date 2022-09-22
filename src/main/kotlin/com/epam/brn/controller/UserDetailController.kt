package com.epam.brn.controller

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.response.BaseSingleObjectResponse
import com.epam.brn.enums.RoleConstants
import com.epam.brn.service.DoctorService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
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
@Api(value = "/users", tags = ["Users"], description = "Contains actions over user details and accounts")
@RolesAllowed(RoleConstants.USER)
class UserDetailController(
    private val userAccountService: UserAccountService,
    private val doctorService: DoctorService,
    private val userAnalyticsService: UserAnalyticsService
) {
    @GetMapping("/search")
    @ApiOperation("Get all users with/without analytic data")
    @RolesAllowed(RoleConstants.ADMIN)
    fun getUsers(
        @RequestParam("withAnalytics", defaultValue = "false") withAnalytics: Boolean,
        @RequestParam("role", defaultValue = "ROLE_USER") role: String,
        @PageableDefault pageable: Pageable,
    ): ResponseEntity<Any> {
        val users = if (withAnalytics) userAnalyticsService.getUsersWithAnalytics(pageable, role)
        else userAccountService.getUsers(pageable, role)
        return ResponseEntity.ok().body(BaseResponse(data = users))
    }

    @GetMapping(value = ["/{userId}"])
    @ApiOperation("Get user by id")
    @RolesAllowed(RoleConstants.ADMIN)
    fun findUserById(@PathVariable("userId") id: Long): ResponseEntity<BaseResponse> {
        return ResponseEntity.ok()
            .body(BaseResponse(data = listOf(userAccountService.findUserById(id))))
    }

    @GetMapping(value = ["/current"])
    @ApiOperation("Get current logged in user")
    fun getCurrentUser() = ResponseEntity.ok()
        .body(BaseResponse(data = listOf(userAccountService.getUserFromTheCurrentSession())))

    @PatchMapping(value = ["/current"])
    @ApiOperation("Update current logged in user")
    fun updateCurrentUser(@Validated @RequestBody userAccountChangeRequest: UserAccountChangeRequest) =
        ResponseEntity.ok()
            .body(BaseSingleObjectResponse(data = userAccountService.updateCurrentUser(userAccountChangeRequest)))

    @GetMapping
    @ApiOperation("Get user by name")
    @RolesAllowed(RoleConstants.ADMIN)
    fun findUserByName(
        @RequestParam("name", required = true) name: String
    ) = ResponseEntity.ok()
        .body(BaseResponse(data = listOf(userAccountService.findUserByName(name))))

    @PutMapping(value = ["/current/avatar"])
    @ApiOperation("Update avatar current user")
    fun updateAvatarCurrentUser(
        @RequestParam("avatar", required = true) avatar: String
    ) = ResponseEntity.ok()
        .body(BaseSingleObjectResponse(data = userAccountService.updateAvatarForCurrentUser(avatar)))

    @PostMapping(value = ["/{userId}/headphones"])
    @ApiOperation("Add headphones to the user")
    @RolesAllowed(RoleConstants.ADMIN)
    fun addHeadphonesToUser(
        @PathVariable("userId", required = true) userId: Long,
        @Validated @RequestBody headphones: HeadphonesDto
    ) = ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseSingleObjectResponse(data = userAccountService.addHeadphonesToUser(userId, headphones)))

    @PostMapping(value = ["/current/headphones"])
    @ApiOperation("Add headphones to current user")
    fun addHeadphonesToCurrentUser(@Validated @RequestBody headphones: HeadphonesDto) =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseSingleObjectResponse(data = userAccountService.addHeadphonesToCurrentUser(headphones)))

    @DeleteMapping(value = ["/current/headphones/{headphonesId}"])
    @ApiOperation("Delete headphone by id")
    fun deleteHeadphonesForCurrentUser(
        @PathVariable(value = "headphonesId") headphonesId: Long
    ): ResponseEntity<BaseSingleObjectResponse> {
        userAccountService.deleteHeadphonesForCurrentUser(headphonesId)
        return ResponseEntity.ok(BaseSingleObjectResponse(data = Unit))
    }

    @GetMapping(value = ["/{userId}/headphones"])
    @ApiOperation("Get all user's headphones")
    @RolesAllowed(RoleConstants.ADMIN)
    fun getAllHeadphonesForUser(
        @PathVariable("userId", required = true) userId: Long
    ) = ResponseEntity
        .ok()
        .body(BaseResponse(data = userAccountService.getAllHeadphonesForUser(userId).toList()))

    @GetMapping(value = ["/current/headphones"])
    @ApiOperation("Get all headphones for current user")
    fun getAllHeadphonesForUser() = ResponseEntity
        .ok()
        .body(BaseResponse(data = userAccountService.getAllHeadphonesForCurrentUser().toList()))

    @GetMapping("/current/{patientId}/doctor")
    @ApiOperation("Get patient's doctor")
    fun getDoctorAssignedToPatient(@PathVariable patientId: Long) =
        ResponseEntity.ok()
            .body(BaseSingleObjectResponse(data = doctorService.getDoctorAssignedToPatient(patientId)))

    @DeleteMapping("/current/{patientId}/doctor")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Delete doctor from patient")
    fun deleteDoctorFromPatient(@PathVariable patientId: Long) =
        doctorService.deleteDoctorFromPatientAsPatient(patientId)
}
