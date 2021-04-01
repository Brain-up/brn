package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.service.UserStatisticService
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *@author Nikolai Lazarev
 */
@RestController
@RequestMapping("/statistics")
@Api(value = "/statistics", description = "User statistic details")
class UserStatisticController(
    @Autowired
    val userStatisticService: UserStatisticService
) {

    @GetMapping("/subgroups")
    fun getUserSubGroupStatistic(
        @RequestParam(value = "ids", required = true) ids: List<Long>
    ): ResponseEntity<BaseResponseDto> {
        val userStatistic = userStatisticService.getSubGroupStatistic(ids)
        return ResponseEntity.ok().body(BaseResponseDto(data = userStatistic))
    }

    @GetMapping("/month")
    fun getUserMonthStatistic(
        @RequestParam(name = "month", required = false) month: Int?,
        @RequestParam(name = "year", required = false) year: Int?
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val monthUserStatistic = userStatisticService.getUserMonthStatistic(month, year)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = monthUserStatistic))
    }

    @GetMapping("/year")
    fun getUserYearStatistic(
        @RequestParam(name = "year", required = false) year: Int?
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val yearUserStatistic = userStatisticService.getUserYearStatistic(year)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = yearUserStatistic))
    }

    @GetMapping("/day")
    fun getUserDayStatistic(
        @RequestParam(name = "month", required = false) month: Int?,
        @RequestParam(name = "year", required = false) year: Int?,
        @RequestParam(name = "day", required = false) day: Int?
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val dayUserStatistic = userStatisticService.getUserDayStatistic(month, day, year)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = dayUserStatistic))
    }
}
