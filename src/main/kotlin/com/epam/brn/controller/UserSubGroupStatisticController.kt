package com.epam.brn.controller

import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.response.SubGroupStatisticsResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.statistics.UserStatisticService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/statistics")
@Tag(name = "Statistics", description = "Contains actions over user statistics details")
@RolesAllowed(BrnRole.USER)
class UserSubGroupStatisticController(
    private val userStatisticService: UserStatisticService<SubGroupStatisticsResponse>,
) {
    @GetMapping("/subgroups")
    @Operation(summary = "Get user's subgroup statistics")
    fun getUserSubGroupStatistic(
        @RequestParam(value = "ids", required = true) ids: List<Long>,
    ): ResponseEntity<BrnResponse<List<SubGroupStatisticsResponse>>> {
        val userStatistic = userStatisticService.getSubGroupStatistic(ids)
        return ResponseEntity.ok().body(BrnResponse(data = userStatistic))
    }
}
