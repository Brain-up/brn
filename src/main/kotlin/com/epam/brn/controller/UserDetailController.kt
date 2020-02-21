package com.epam.brn.controller

import com.epam.brn.constant.BrnParams
import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.service.UserAccountService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.USERS)
@Api(value = BrnPath.USERS, description = "Contains actions over user details and accounts")
class UserDetailController(@Autowired val userAccountService: UserAccountService) {

    @PostMapping
    fun addUser(@Validated @RequestBody userAccountDto: UserAccountDto): ResponseEntity<BaseResponseDto> {
        val newUser = userAccountService.addUser(userAccountDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseDto(data = listOf(newUser)))
    }

    @GetMapping(value = ["/{${BrnParams.USER_ID}}"])
    @ApiOperation("Get user by Id")
    fun findUserById(@PathVariable(BrnParams.USER_ID) id: Long): ResponseEntity<BaseResponseDto> {
        return ResponseEntity.ok()
            .body(BaseResponseDto(data = listOf(userAccountService.findUserById(id))))
    }

    @GetMapping(value = ["/${BrnParams.CURRENT_USER}"])
    @ApiOperation("Get current logged in user")
    fun getCurrentUser() = ResponseEntity.ok()
        .body(BaseResponseDto(data = listOf(userAccountService.getUserFromTheCurrentSession())))

    @DeleteMapping(value = ["/{${BrnParams.USER_ID}}"])
    @ApiOperation("Delete a user from the system")
    fun deleteUser(@PathVariable(BrnParams.USER_ID) id: Long): ResponseEntity<BaseResponseDto> {
        userAccountService.removeUserWithId(id)
        return ResponseEntity.ok(BaseResponseDto(data = listOf("Successfully deleted")))
    }

    @GetMapping
    @ApiOperation("Get user by name")
    fun findUserByName(
        @RequestParam(BrnParams.USER_FIRST_NAME, required = true) firstName: String,
        @RequestParam(BrnParams.USER_LAST_NAME, required = true) lastName: String
    ) = ResponseEntity.ok()
        .body(BaseResponseDto(data = listOf(userAccountService.findUserByName(firstName, lastName))))
}
