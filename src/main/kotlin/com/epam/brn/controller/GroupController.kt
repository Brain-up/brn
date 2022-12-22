package com.epam.brn.controller

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.ExerciseGroupsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/groups")
@Tag(name = "Groups", description = "Contains actions over groups")
@RolesAllowed(BrnRole.USER)
class GroupController(val exerciseGroupsService: ExerciseGroupsService) {

    // The discrepancy in naming with "Groups" endpoint and "ExerciseGroup" entity is due to
    // group being a reserved word in db.
    @GetMapping
    @Operation(summary = "Get all groups by locale if it was set")
    fun getGroups(@RequestParam(value = "locale", required = false, defaultValue = "ru-ru") locale: String): ResponseEntity<BrnResponse<List<ExerciseGroupDto>>> {
        return ResponseEntity.ok().body(BrnResponse(data = exerciseGroupsService.findByLocale(locale)))
    }

    @GetMapping(value = ["/{groupId}"])
    @Operation(summary = "Get group by id")
    fun getGroupById(@PathVariable("groupId") groupId: Long): ResponseEntity<BrnResponse<ExerciseGroupDto>> {
        return ResponseEntity.ok()
            .body(BrnResponse(data = exerciseGroupsService.findGroupDtoById(groupId)))
    }
}
