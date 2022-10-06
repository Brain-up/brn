package com.epam.brn.controller

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.response.AuthorityResponse
import com.epam.brn.dto.response.Response
import com.epam.brn.enums.BrnRole
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/roles")
@Api(value = "/roles", tags = ["Roles"], description = "Contains actions over roles")
@RolesAllowed(BrnRole.ADMIN)
class RoleController(val authorityService: AuthorityService) {

    @GetMapping
    @ApiOperation("Get all roles")
    fun getRoles(): ResponseEntity<Response<List<AuthorityResponse>>> {
        val authorities = authorityService.findAll()
        return ResponseEntity.ok().body(Response(data = authorities))
    }
}
