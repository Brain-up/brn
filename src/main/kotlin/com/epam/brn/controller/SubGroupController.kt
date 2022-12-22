package com.epam.brn.controller

import com.epam.brn.dto.request.SubGroupChangeRequest
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.response.SubGroupResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.SubGroupService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Sub Groups", description = "Contains actions over subgroup")
@RolesAllowed(BrnRole.USER)
class SubGroupController(private val subGroupsService: SubGroupService) {

    @GetMapping
    @Operation(summary = "Get subgroups for series")
    fun getAllGroups(@RequestParam(value = "seriesId", required = true) seriesId: Long): ResponseEntity<BrnResponse<List<SubGroupResponse>>> {
        val data = subGroupsService.findSubGroupsForSeries(seriesId)
        return ResponseEntity.ok().body(BrnResponse(data = data))
    }

    @GetMapping("{subGroupId}")
    @Operation(summary = "Get subgroup for id")
    fun getSeriesForId(@PathVariable(value = "subGroupId") subGroupId: Long): ResponseEntity<BrnResponse<SubGroupResponse>> {
        val subGroupDto = subGroupsService.findById(subGroupId)
        return ResponseEntity.ok(BrnResponse(data = subGroupDto))
    }

    @DeleteMapping("{subGroupId}")
    @Operation(summary = "Delete subgroup by id without exercises")
    fun deleteSubGroupById(@PathVariable(value = "subGroupId") subGroupId: Long): ResponseEntity<BrnResponse<Unit>> {
        subGroupsService.deleteSubGroupById(subGroupId)
        return ResponseEntity.ok(BrnResponse(data = Unit))
    }

    @PostMapping
    @Operation(summary = "Add new subgroup for existing series")
    @RolesAllowed(BrnRole.ADMIN)
    fun addSubGroupToSeries(
        @Parameter(name = "seriesId", description = "ID of existed series", example = "1")
        @RequestParam(value = "seriesId") seriesId: Long,
        @Valid @RequestBody subGroupRequest: SubGroupRequest
    ): ResponseEntity<BrnResponse<SubGroupResponse>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(BrnResponse(data = subGroupsService.addSubGroupToSeries(subGroupRequest, seriesId)))

    @PatchMapping("/{subGroupId}")
    @Operation(summary = "Update subgroup by id")
    @RolesAllowed(BrnRole.ADMIN)
    fun updateSubGroupById(
        @PathVariable(value = "subGroupId") subGroupId: Long,
        @RequestBody subGroup: SubGroupChangeRequest
    ): ResponseEntity<BrnResponse<SubGroupResponse>> =
        ResponseEntity.ok(BrnResponse(data = subGroupsService.updateSubGroupById(subGroupId, subGroup)))
}
