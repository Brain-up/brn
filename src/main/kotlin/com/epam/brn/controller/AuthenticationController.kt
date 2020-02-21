package com.epam.brn.controller

import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.AuthOutDto
import com.epam.brn.dto.LoginDto
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.service.AuthenticationService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
@Api(description = "Contains login in actions")
class AuthenticationController(
    val authenticationService: AuthenticationService,
    val authenticationManager: AuthenticationManager
//    @Autowired val repository: SecurityContextRepository,
//    @Autowired val rememberMeServices: RememberMeServices
) {

    @PostMapping(BrnPath.REGISTRATION)
    @ApiOperation("New user registration")
    fun registration(@Validated @RequestBody userAccountDto: UserAccountDto): ResponseEntity<AuthOutDto> {
        val token: UsernamePasswordAuthenticationToken = authenticationService.registration(userAccountDto)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(AuthOutDto(token.toString()))
    }

    @PostMapping(BrnPath.LOGIN)
    @ApiOperation("Exist user login")
    fun login(
        @Validated @RequestBody loginDto: LoginDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<AuthOutDto> {
        val token: UsernamePasswordAuthenticationToken = authenticationService.login(loginDto)
        val auth = authenticationManager.authenticate(token)
        SecurityContextHolder.getContext().authentication = auth
//        repository.saveContext(SecurityContextHolder.getContext(), request, response)
//        rememberMeServices.loginSuccess(request, response, auth)
        return ResponseEntity
            .ok()
            .body(AuthOutDto(token.toString()))
    }
}
