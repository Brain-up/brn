package com.epam.brn.controller

import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.response.RoleResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.RoleService
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
@RolesAllowed(BrnRole.ADMIN, BrnRole.SPECIALIST)
class RoleController(val roleService: RoleService) {

    @GetMapping
    @ApiOperation("Get all roles")
    fun getRoles(): ResponseEntity<BrnResponse<List<RoleResponse>>> {
        val roles = roleService.findAll().map { role -> role.toDto() }
        return ResponseEntity.ok().body(BrnResponse(data = roles))
    }
}
