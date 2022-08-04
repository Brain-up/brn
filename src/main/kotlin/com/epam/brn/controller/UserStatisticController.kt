package com.epam.brn.controller

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.response.SubGroupStatisticResponse
import com.epam.brn.service.statistic.UserStatisticService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/statistics")
@Api(value = "/statistics", description = "Contains actions over user statistic details")
class UserStatisticController(
    private val userStatisticService: UserStatisticService<SubGroupStatisticResponse>,
) {

    @GetMapping("/subgroups")
    @ApiOperation("Get user's subgroup statistics")
    fun getUserSubGroupStatistic(
        @RequestParam(value = "ids", required = true) ids: List<Long>
    ): ResponseEntity<BaseResponse> {
        val userStatistic = userStatisticService.getSubGroupStatistic(ids)
        return ResponseEntity.ok().body(BaseResponse(data = userStatistic))
    }
}
