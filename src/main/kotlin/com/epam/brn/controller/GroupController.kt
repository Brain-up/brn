package com.epam.brn.controller

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.response.BaseSingleObjectResponse
import com.epam.brn.enums.RoleConstants
import com.epam.brn.service.ExerciseGroupsService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/groups")
@Api(value = "/groups", tags = ["Groups"], description = "Contains actions over groups")
@RolesAllowed(RoleConstants.USER)
class GroupController(val exerciseGroupsService: ExerciseGroupsService) {

    // The discrepancy in naming with "Groups" endpoint and "ExerciseGroup" entity is due to
    // group being a reserved word in db.
    @GetMapping
    @ApiOperation("Get all groups by locale if it was set")
    fun getGroups(@RequestParam(value = "locale", required = false, defaultValue = "ru-ru") locale: String): ResponseEntity<BaseResponse> {
        return ResponseEntity.ok().body(BaseResponse(data = exerciseGroupsService.findByLocale(locale)))
    }

    @GetMapping(value = ["/{groupId}"])
    @ApiOperation("Get group by id")
    fun getGroupById(@PathVariable("groupId") groupId: Long): ResponseEntity<BaseSingleObjectResponse> {
        return ResponseEntity.ok()
            .body(BaseSingleObjectResponse(data = exerciseGroupsService.findGroupDtoById(groupId)))
    }
}
