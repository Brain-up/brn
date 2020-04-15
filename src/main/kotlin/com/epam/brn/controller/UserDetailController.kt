package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.UserAccountDto
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import java.security.Principal
import org.keycloak.KeycloakPrincipal
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
@Api(value = "/users", description = "Contains actions over user details and accounts")
class UserDetailController(private val keycloak: Keycloak) {

    @PostMapping
    fun addUser(@Validated @RequestBody userAccountDto: UserAccountDto): ResponseEntity<BaseResponseDto> {
        val rep = UserRepresentation()
        rep.email = userAccountDto.email
        rep.username = userAccountDto.username
        rep.lastName = userAccountDto.lastName
        rep.firstName = userAccountDto.firstName
        rep.attributes = mutableMapOf("birthDate" to listOf("19.02.1978"), "gender" to listOf("m"))
        val create = keycloak.realm("brn").users().create(rep)
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponseDto(data = listOf(create)))
    }

    @GetMapping(value = ["/current"])
    @ApiOperation("Get current logged in user")
    fun getCurrentUser(principal: Principal): ResponseEntity<BaseResponseDto> {

        val kp = principal as KeycloakPrincipal<*>
        val token = kp.keycloakSecurityContext.token
        val userData = UserData(token.id)
        userData.userName = token.name
        userData.attributes = token.otherClaims
        userData.birthDate = token.birthdate
        return ResponseEntity.ok()
            .body(BaseResponseDto(data = listOf(userData)))
    }

    private data class UserData(
        val id: String
    ) {
        var userName: String = ""
        var lastName: String = ""
        var birthDate: String = ""
        var attributes: Map<String, Any> = mapOf()
    }
}
