package com.epam.brn.controller

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.UserDailyDetailStatisticsDto
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v2/statistics")
@Api(value = "/statistics", description = "Contains actions over user statistic details")
class UserStatisticControllerV2(
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>,
    private val historyService: StudyHistoryService
) {

    @GetMapping("/study/week")
    @ApiOperation("Get user's weekly statistic for period. Where period is a two dates in the ISO date time format")
    fun getUserWeeklyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime
    ): ResponseEntity<BaseResponse<List<DayStudyStatistic>>> {
        val result = userDayStatisticService.getStatisticForPeriod(from, to)
        return ResponseEntity.ok().body(BaseResponse(data = result))
    }

    @GetMapping("/study/year")
    @ApiOperation("Get user's yearly statistic for period. Where period is a two dates in the ISO date time format")
    fun getUserYearlyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime,
    ): ResponseEntity<BaseResponse<List<MonthStudyStatistic>>> {
        val result = userMonthStatisticService.getStatisticForPeriod(from, to)
        return ResponseEntity.ok().body(BaseResponse(data = result))
    }

    @GetMapping("/study/day")
    @ApiOperation("Get current user's details daily statistic for day. Where day is a date in the ISO date time format")
    fun getUserDailyDetailsStatistics(
        @RequestParam(name = "day", required = true) day: LocalDateTime
    ): ResponseEntity<BaseResponse<List<UserDailyDetailStatisticsDto>>> {
        val result = historyService.getUserDailyStatistics(day = day)
        return ResponseEntity.ok().body(BaseResponse(data = result))
    }
}
