package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.service.UserAccountService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
@Api(value = "/users", description = "Contains actions over user details and accounts")
class UserDetailController(@Autowired val userAccountService: UserAccountService) {

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
}
