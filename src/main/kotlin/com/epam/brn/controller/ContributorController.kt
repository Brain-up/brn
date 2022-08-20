package com.epam.brn.controller

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.enums.ContributorType
import com.epam.brn.service.ContributorService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contributor")
@Api(value = "/contributor", description = "Contains actions over contributors of this project")
class ContributorController(@Autowired val contributorService: ContributorService) {

    @GetMapping
    @ApiOperation("Get all contributors by type")
    fun getContributors(
        @RequestParam(name = "locale", required = false, defaultValue = "ru-ru") locale: String,
        @RequestParam(name = "type") type: ContributorType,
    ): ResponseEntity<BaseResponse> {
        return ResponseEntity.ok()
            .body(BaseResponse(data = contributorService.getContributors(locale, type)))
    }
}
