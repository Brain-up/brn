package com.epam.brn.controller

import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.AuthOutDto
import com.epam.brn.dto.LoginDto
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.service.AuthenticationService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
@Api(description = "Contains login in actions")
class AuthenticationController(val authenticationService: AuthenticationService) {

    @PostMapping(BrnPath.REGISTRATION)
    @ApiOperation("New user registration")
    fun registration(@Validated @RequestBody userAccountDto: UserAccountDto): ResponseEntity<AuthOutDto> {
        val basicHeader = authenticationService.registration(userAccountDto)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(AuthOutDto(basicHeader))
    }

    @PostMapping(BrnPath.LOGIN)
    @ApiOperation("Exist user login")
    fun login(@Validated @RequestBody loginDto: LoginDto): ResponseEntity<AuthOutDto> {
        val basicHeader = authenticationService.login(loginDto)
        return ResponseEntity
            .ok()
            .body(AuthOutDto(basicHeader))
    }
}
