package com.epam.brn.controller

import com.epam.brn.auth.AuthenticationService
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.google.firebase.auth.FirebaseAuth
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
@RequestMapping("/registration")
@Api(description = "Contains registration actions")
class AuthenticationController(
    private val authenticationService: AuthenticationService,
    private val firebaseAuth: FirebaseAuth
) {

    @PostMapping()
    @ApiOperation("New email\\password user registration")
    fun registration(
        @Validated @RequestBody userAccountCreateRequest: UserAccountCreateRequest
    ): ResponseEntity<UserAccountResponse> {
        val newUser = authenticationService.registration(userAccountCreateRequest)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(newUser)
    }
}
