package com.epam.brn.controller

import com.epam.brn.dto.request.contributor.ContributorRequest
import com.epam.brn.dto.response.ContributorResponse
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.enums.ContributorType
import com.epam.brn.service.ContributorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(
    name = "Contributors",
    description = "Contains actions over contributors of this project"
)
class ContributorController(val contributorService: ContributorService) {

    @GetMapping
    @Operation(summary = "Get all contributors by type")
    fun getContributors(
        @RequestParam(name = "locale", required = false, defaultValue = "ru-ru") locale: String,
        @RequestParam(name = "type", required = false) type: ContributorType?,
    ): ResponseEntity<BrnResponse<List<ContributorResponse>>> = ResponseEntity.ok()
        .body(
            BrnResponse(
                data = if (type == null)
                    contributorService.getAllContributors()
                else
                    contributorService.getContributors(locale, type)
            )
        )

    @PostMapping
    @Operation(summary = "Add a new contributor")
    @RolesAllowed(BrnRole.ADMIN)
    fun createContributor(
        @Parameter(description = "Contributor data", required = true)
        @Valid @RequestBody contributorDto: ContributorRequest
    ): ResponseEntity<BrnResponse<ContributorResponse>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(BrnResponse(data = contributorService.createContributor(contributorDto)))

    @PutMapping("/{contributorId}")
    @Operation(summary = "Update an existing contributor")
    @RolesAllowed(BrnRole.ADMIN)
    fun updateContributor(
        @PathVariable("contributorId") contributorId: Long,
        @Parameter(description = "Contributor data", required = true)
        @Valid @RequestBody contributorDto: ContributorRequest
    ): ResponseEntity<BrnResponse<ContributorResponse>> =
        ResponseEntity.ok()
            .body(BrnResponse(data = contributorService.updateContributor(contributorId, contributorDto)))
}
