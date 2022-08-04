package com.epam.brn.controller

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.response.BaseSingleObjectResponse
import com.epam.brn.dto.response.SubGroupStatisticResponse
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.service.statistic.UserStatisticService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@RestController
@RequestMapping("/statistics")
@Api(value = "/statistics", description = "Contains actions over user statistic details")
class UserStatisticController(
    private val userStatisticService: UserStatisticService<SubGroupStatisticResponse>,
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>
) {

    @GetMapping("/subgroups")
    @ApiOperation("Get user's subgroup statistics")
    fun getUserSubGroupStatistic(
        @RequestParam(value = "ids", required = true) ids: List<Long>
    ): ResponseEntity<BaseResponse> {
        val userStatistic = userStatisticService.getSubGroupStatistic(ids)
        return ResponseEntity.ok().body(BaseResponse(data = userStatistic))
    }

    @GetMapping("/study/year")
    @ApiOperation("Get user's yearly statistic for period. Where period is a two dates in the format yyyy-MM-dd")
    @Deprecated(
        message = "Use the method with LocalDateTime as the dates type instead",
        replaceWith = ReplaceWith("getUserYearlyStatistic(from, to)", imports = ["com.epam.brn.controller.UserStatisticControllerV2"])
    )
    fun getUserYearlyStatistic(
        @RequestParam(name = "from", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") from: LocalDate,
        @RequestParam(name = "to", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") to: LocalDate
    ): ResponseEntity<BaseSingleObjectResponse> {
        val tempFrom = LocalDateTime.of(from, LocalTime.MIN)
        val tempTo = LocalDateTime.of(to, LocalTime.MAX)
        val result = userMonthStatisticService.getStatisticForPeriod(tempFrom, tempTo)
        val response = result.map {
            it.toDto()
        }
        return ResponseEntity.ok().body(BaseSingleObjectResponse(data = response))
    }
}
