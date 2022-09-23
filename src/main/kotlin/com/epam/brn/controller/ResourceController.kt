package com.epam.brn.controller

import com.epam.brn.dto.ResourceDto
import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.dto.response.Response
import com.epam.brn.enums.RoleConstants
import com.epam.brn.service.ResourceService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/resources")
@Api(value = "/resources", tags = ["Resources"], description = "Contains actions over resources")
@RolesAllowed(RoleConstants.ADMIN)
class ResourceController(val resourceService: ResourceService) {

    @PatchMapping("/resources/{id}")
    @ApiOperation("Update resource description by resource id")
    fun updateResourceDescription(
        @PathVariable(value = "id") id: Long,
        @RequestBody @Validated request: UpdateResourceDescriptionRequest
    ): ResponseEntity<Response<ResourceDto>> =
        ResponseEntity.ok()
            .body(Response(data = resourceService.updateDescription(id, request.description!!)))
}
