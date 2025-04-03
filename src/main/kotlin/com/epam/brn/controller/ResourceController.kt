package com.epam.brn.controller

import com.epam.brn.dto.response.ResourceResponse
import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.dto.request.contributor.ContributorRequest
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.response.ContributorResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.enums.ContributorType
import com.epam.brn.job.ResourcePictureUrlUpdateJob
import com.epam.brn.job.ResourcePictureUrlUpdateJobResponse
import com.epam.brn.model.Resource
import com.epam.brn.service.ResourceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.annotation.security.RolesAllowed
import javax.validation.Valid

@RestController
@RequestMapping("/resources")
@Tag(name = "Resources", description = "Contains actions over resources")
@RolesAllowed(BrnRole.ADMIN)
class ResourceController(
    val resourceService: ResourceService,
    val resourcePictureUpdateJob: ResourcePictureUrlUpdateJob
) {

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
    fun updateResourceUrls(): ResponseEntity<ResourcePictureUrlUpdateJobResponse> {
        return ResponseEntity.ok(resourcePictureUpdateJob.updatePictureUrl())
    }

    @GetMapping
    @Operation(summary = "Get all resources")
    fun getResources(): ResponseEntity<BrnResponse<List<ResourceResponse>>> = ResponseEntity.ok()
        .body(
            BrnResponse(resourceService.findAll().map { map -> map.toResponse() })
        )

    @PostMapping
    @Operation(summary = "Add a new Resource")
    @RolesAllowed(BrnRole.ADMIN)
    fun createResource(
        @Parameter(description = "Resource data", required = true)
        @Valid @RequestBody resourceDto: Resource
    ): ResponseEntity<BrnResponse<ResourceResponse>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(BrnResponse(resourceService.save(resourceDto).toResponse()))

}
