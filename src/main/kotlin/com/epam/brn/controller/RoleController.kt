package com.epam.brn.controller

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.enums.RoleConstants
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
@RolesAllowed(RoleConstants.ADMIN)
class RoleController(val authorityService: AuthorityService) {

    @GetMapping
    @ApiOperation("Get all roles")
    fun getRoles(): ResponseEntity<BaseResponse> {
        val authorities = authorityService.findAll()
        return ResponseEntity.ok().body(BaseResponse(data = authorities))
    }
}
