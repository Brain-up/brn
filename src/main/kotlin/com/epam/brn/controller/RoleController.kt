package com.epam.brn.controller

import com.epam.brn.service.RoleService
import com.epam.brn.dto.response.RoleResponse
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/roles")
@Tag(name = "Roles", description = "Contains actions over roles")
@RolesAllowed(BrnRole.ADMIN)
class RoleController(val roleService: RoleService) {

    @GetMapping
    @Operation(summary = "Get all roles")
    fun getRoles(): ResponseEntity<BrnResponse<List<RoleResponse>>> {
        val roles = roleService.findAll().map { role -> role.toDto() }
        return ResponseEntity.ok().body(BrnResponse(data = roles))
    }
}
