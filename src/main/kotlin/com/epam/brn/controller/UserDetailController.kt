package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.service.UserAccountService
import com.google.firebase.auth.FirebaseAuth
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
@Api(value = "/users", description = "Contains actions over user details and accounts")
class UserDetailController(
    private val userAccountService: UserAccountService,
    private val firebaseAuth: FirebaseAuth
) {

    @GetMapping(value = ["/{userId}"])
    @ApiOperation("Get user by Id")
    fun findUserById(@PathVariable("userId") id: Long): ResponseEntity<BaseResponseDto> {
        return ResponseEntity.ok()
            .body(BaseResponseDto(data = listOf(userAccountService.findUserById(id))))
    }

    @GetMapping(value = ["/current"])
    @ApiOperation("Get current logged in user")
    fun getCurrentUser() = ResponseEntity.ok()
        .body(BaseResponseDto(data = listOf(userAccountService.getUserFromTheCurrentSession())))

    @PatchMapping(value = ["/current"])
    @ApiOperation("Update current logged in user")
    fun updateCurrentUser(@Validated @RequestBody userAccountChangeRequest: UserAccountChangeRequest) =
        ResponseEntity.ok()
            .body(BaseSingleObjectResponseDto(data = userAccountService.updateCurrentUser(userAccountChangeRequest)))

    @GetMapping
    @ApiOperation("Get user by name")
    fun findUserByName(
        @RequestParam("name", required = true) name: String
    ) = ResponseEntity.ok()
        .body(BaseResponseDto(data = listOf(userAccountService.findUserByName(name))))

    @PutMapping(value = ["/current/avatar"])
    @ApiOperation("Update avatar current user")
    fun updateAvatarCurrentUser(
        @RequestParam("avatar", required = true) avatar: String
    ) = ResponseEntity.ok()
        .body(BaseSingleObjectResponseDto(data = userAccountService.updateAvatarForCurrentUser(avatar)))

    @PostMapping(value = ["/{userId}/headphones"])
    @ApiOperation("Add headphones to the user")
    fun addHeadphonesToUser(
        @PathVariable("userId", required = true) userId: Long,
        @Validated @RequestBody headphones: HeadphonesDto
    ) = ResponseEntity
        .status(HttpStatus.CREATED)
        .body(BaseSingleObjectResponseDto(data = userAccountService.addHeadphonesToUser(userId, headphones)))

    @PostMapping(value = ["/current/headphones"])
    @ApiOperation("Add headphones to current user")
    fun addHeadphonesToCurrentUser(@Validated @RequestBody headphones: HeadphonesDto) =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseSingleObjectResponseDto(data = userAccountService.addHeadphonesToCurrentUser(headphones)))

    @GetMapping(value = ["/{userId}/headphones"])
    @ApiOperation("Get all user's headphones")
    fun getAllHeadphonesForUser(
        @PathVariable("userId", required = true) userId: Long
    ) = ResponseEntity
        .ok()
        .body(BaseResponseDto(data = userAccountService.getAllHeadphonesForUser(userId).toList()))

    @GetMapping(value = ["/current/headphones"])
    @ApiOperation("Get all headphones for current user")
    fun getAllHeadphonesForUser() = ResponseEntity
        .ok()
        .body(BaseResponseDto(data = userAccountService.getAllHeadphonesForCurrentUser().toList()))
}
