package com.epam.brn.controller

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.response.AuthorityResponse
import com.epam.brn.dto.response.Response
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
@Api(value = "/admin", description = "Contains actions for admin to work with users")
class AdminUserFlowController(
    private val userAccountService: UserAccountService,
    private val userAnalyticsService: UserAnalyticsService,
    private val authorityService: AuthorityService
) {

    @GetMapping("/users")
    @ApiOperation("Get all users with/without analytic data")
    fun getUsers(
        @RequestParam("withAnalytics", defaultValue = "false") withAnalytics: Boolean,
        @RequestParam("role", defaultValue = "ROLE_USER") role: String,
        @PageableDefault pageable: Pageable,
    ): ResponseEntity<Response<List<Any>>> {
        val users = if (withAnalytics) userAnalyticsService.getUsersWithAnalytics(pageable, role)
        else userAccountService.getUsers(pageable, role)
        return ResponseEntity.ok().body(Response(data = users))
    }

    @GetMapping("/roles")
    @ApiOperation("Get all roles")
    fun getRoles(): ResponseEntity<Response<List<AuthorityResponse>>> {
        val authorities = authorityService.findAll()
        return ResponseEntity.ok().body(Response(data = authorities))
    }
}
