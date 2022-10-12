package com.epam.brn.controller

import com.epam.brn.dto.request.contributor.ContributorRequest
import com.epam.brn.dto.response.ContributorResponse
import com.epam.brn.dto.response.Response
import com.epam.brn.enums.BrnRole
import com.epam.brn.enums.ContributorType
import com.epam.brn.service.ContributorService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed
import javax.validation.Valid

@RestController
@RequestMapping("/contributors")
@Api(
    value = "/contributors",
    tags = ["Contributors"],
    description = "Contains actions over contributors of this project"
)
class ContributorController(val contributorService: ContributorService) {

    @GetMapping
    @ApiOperation("Get all contributors by type")
    fun getContributors(
        @RequestParam(name = "locale", required = false, defaultValue = "ru-ru") locale: String,
        @RequestParam(name = "type", required = false) type: ContributorType?,
    ): ResponseEntity<Response<List<ContributorResponse>>> {
        return ResponseEntity.ok()
            .body(
                Response(
                    data = if (type == null)
                        contributorService.getAllContributors()
                    else
                        contributorService.getContributors(locale, type!!)
                )
            )
    }

    @PostMapping
    @ApiOperation("Add a new contributor")
    @RolesAllowed(BrnRole.ADMIN)
    fun createContributor(
        @ApiParam(value = "Contributor data", required = true)
        @Valid @RequestBody contributorDto: ContributorRequest
    ): ResponseEntity<Response<ContributorResponse>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(data = contributorService.createContributor(contributorDto)))

    @PutMapping("/{contributorId}")
    @ApiOperation("Update an existing contributor")
    @RolesAllowed(BrnRole.ADMIN)
    fun updateContributor(
        @PathVariable("contributorId") contributorId: Long,
        @ApiParam(value = "Contributor data", required = true)
        @Valid @RequestBody contributorDto: ContributorRequest
    ): ResponseEntity<Response<ContributorResponse>> =
        ResponseEntity.ok()
            .body(Response(data = contributorService.updateContributor(contributorId, contributorDto)))
}
