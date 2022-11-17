package com.epam.brn.controller

import com.epam.brn.dto.request.SubGroupChangeRequest
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.response.SubGroupResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.SubGroupService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed
import javax.validation.Valid

@RestController
@RequestMapping("/subgroups")
@Api(value = "/subgroups", tags = ["Sub Groups"], description = "Contains actions over subgroup")
@RolesAllowed(BrnRole.USER)
class SubGroupController(private val subGroupsService: SubGroupService) {

    @GetMapping
    @ApiOperation("Get subgroups for series")
    fun getAllGroups(@RequestParam(value = "seriesId", required = true) seriesId: Long): ResponseEntity<BrnResponse<List<SubGroupResponse>>> {
        val data = subGroupsService.findSubGroupsForSeries(seriesId)
        return ResponseEntity.ok().body(BrnResponse(data = data))
    }

    @GetMapping("{subGroupId}")
    @ApiOperation("Get subgroup for id")
    fun getSeriesForId(@PathVariable(value = "subGroupId") subGroupId: Long): ResponseEntity<BrnResponse<SubGroupResponse>> {
        val subGroupDto = subGroupsService.findById(subGroupId)
        return ResponseEntity.ok(BrnResponse(data = subGroupDto))
    }

    @DeleteMapping("{subGroupId}")
    @ApiOperation("Delete subgroup by id without exercises")
    fun deleteSubGroupById(@PathVariable(value = "subGroupId") subGroupId: Long): ResponseEntity<BrnResponse<Unit>> {
        subGroupsService.deleteSubGroupById(subGroupId)
        return ResponseEntity.ok(BrnResponse(data = Unit))
    }

    @PostMapping
    @ApiOperation("Add new subgroup for existing series")
    @RolesAllowed(BrnRole.ADMIN)
    fun addSubGroupToSeries(
        @ApiParam(name = "seriesId", type = "Long", value = "ID of existed series", example = "1")
        @RequestParam(value = "seriesId") seriesId: Long,
        @Valid @RequestBody subGroupRequest: SubGroupRequest
    ): ResponseEntity<BrnResponse<SubGroupResponse>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(BrnResponse(data = subGroupsService.addSubGroupToSeries(subGroupRequest, seriesId)))

    @PatchMapping("/{subGroupId}")
    @ApiOperation("Update subgroup by id")
    @RolesAllowed(BrnRole.ADMIN)
    fun updateSubGroupById(
        @PathVariable(value = "subGroupId") subGroupId: Long,
        @RequestBody subGroup: SubGroupChangeRequest
    ): ResponseEntity<BrnResponse<SubGroupResponse>> =
        ResponseEntity.ok(BrnResponse(data = subGroupsService.updateSubGroupById(subGroupId, subGroup)))
}
