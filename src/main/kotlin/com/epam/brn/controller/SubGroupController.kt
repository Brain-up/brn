package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.service.SubGroupService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/subgroups")
@Api(value = "/subgroups", description = "Contains actions over subgroups")
class SubGroupController(private val subGroupsService: SubGroupService) {

    @GetMapping
    @ApiOperation("Get subGroups for series id.")
    fun getAllGroups(@RequestParam(value = "seriesId", required = true) seriesId: Long): ResponseEntity<BaseResponseDto> {
        val data = subGroupsService.findSubGroupsForSeries(seriesId)
        return ResponseEntity.ok().body(BaseResponseDto(data = data))
    }

    @GetMapping("{subGroupId}")
    @ApiOperation("Get subGroup for id.")
    fun getSeriesForId(@PathVariable(value = "subGroupId") subGroupId: Long): ResponseEntity<BaseSingleObjectResponseDto> {
        val subGroupDto = subGroupsService.findById(subGroupId)
        return ResponseEntity.ok(BaseSingleObjectResponseDto(data = subGroupDto))
    }

    @DeleteMapping("{subGroupId}")
    @ApiOperation("Delete series for id.")
    fun deleteSubGroupForId(@PathVariable(value = "subGroupId") subGroupId: Long) {
        subGroupsService.deleteSubGroupById(subGroupId)
    }
}
