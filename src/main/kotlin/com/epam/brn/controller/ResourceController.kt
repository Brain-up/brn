package com.epam.brn.controller

import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.response.ResourceResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.job.ResourcePictureUrlUpdateJob
import com.epam.brn.job.ResourcePictureUrlUpdateJobResponse
import com.epam.brn.service.ResourceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import javax.annotation.security.RolesAllowed
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/resources")
@Tag(name = "Resources", description = "Contains actions over resources")
@RolesAllowed(BrnRole.ADMIN)
class ResourceController(
    val resourceService: ResourceService,
    val resourcePictureUpdateJob: ResourcePictureUrlUpdateJob
) {

    @GetMapping
    @Operation(summary = "Get all resources")
    fun getResources(): ResponseEntity<List<ResourceResponse>> =
        ResponseEntity.ok(resourceService.findAll().sortedBy { it.word }.map { it.toResponse() })

    @PatchMapping("/{id}")
    @Operation(summary = "Update resource description by resource id")
    fun updateResourceDescription(
        @PathVariable(value = "id") id: Long,
        @RequestBody @Validated request: UpdateResourceDescriptionRequest
    ): ResponseEntity<BrnResponse<ResourceResponse>> =
        ResponseEntity.ok()
            .body(BrnResponse(data = resourceService.updateDescription(id, request.description!!)))

    @PostMapping("/update")
    @Operation(summary = "Update picture URL for all resources")
    fun updateResourceUrls(): ResponseEntity<ResourcePictureUrlUpdateJobResponse> =
        ResponseEntity.ok(resourcePictureUpdateJob.updatePictureUrl())
}
