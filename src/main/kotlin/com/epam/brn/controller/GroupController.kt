package com.epam.brn.controller

import com.epam.brn.constant.BrnParams.GROUP_ID
import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.service.ExerciseGroupsService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.GROUPS)
@Api(value = BrnPath.GROUPS, description = "Contains actions over groups")
class GroupController(@Autowired val exerciseGroupsService: ExerciseGroupsService) {

    // The discrepancy in naming with "Groups" endpoint and "ExerciseGroup" entity is due to
    // group being a reserved word in db.
    @GetMapping
    @ApiOperation("Get all groups")
    fun getAllGroups(): ResponseEntity<BaseResponseDto> {
        return ResponseEntity.ok().body(BaseResponseDto(data = exerciseGroupsService.findAllGroups()))
    }

    @GetMapping(value = ["/{$GROUP_ID}"])
    @ApiOperation("Get group by id")
    fun getGroupById(
        @PathVariable(GROUP_ID) groupId: Long
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = exerciseGroupsService.findGroupById(groupId)))
    }
}