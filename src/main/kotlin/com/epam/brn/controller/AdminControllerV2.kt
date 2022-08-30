package com.epam.brn.controller

import com.epam.brn.dto.response.Response
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
@RequestMapping("/v2/admin")
@Api(value = "/v2/admin", description = "Contains actions for admin")
class AdminControllerV2(
    private val studyHistoryService: StudyHistoryService,
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>,
) {

    @GetMapping("/histories")
    @ApiOperation("Get user's study histories for period from <= startTime <= to where startTime is a date in ISO date time format")
    fun getHistories(
        @RequestParam("userId", required = true) userId: Long,
        @RequestParam("from", required = true) from: LocalDateTime,
        @RequestParam("to", required = true) to: LocalDateTime
    ) = ResponseEntity.ok()
        .body(Response(data = studyHistoryService.getHistories(userId, from, to)))

    @GetMapping("/study/day")
    @ApiOperation("Get user's details daily statistic for the day. Where day is a date in the ISO date time format")
    fun getUserDailyDetailsStatistics(
        @RequestParam(name = "day", required = true) day: LocalDateTime,
        @RequestParam(name = "userId", required = true) userId: Long
    ): ResponseEntity<Response<List<UserDailyDetailStatisticsDto>>> {
        val result = studyHistoryService.getUserDailyStatistics(day, userId)
        return ResponseEntity.ok().body(Response(data = result))
    }

    @GetMapping("/study/week")
    @ApiOperation("Get user's weekly statistic for the period. Where period is a two dates in the ISO date time format")
    fun getUserWeeklyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime,
        @RequestParam(name = "userId", required = true) userId: Long
    ): ResponseEntity<Response<List<DayStudyStatistic>>> {
        val result = userDayStatisticService.getStatisticForPeriod(from, to, userId)
        return ResponseEntity.ok().body(Response(data = result))
    }

    @GetMapping("/study/year")
    @ApiOperation("Get user's yearly statistic for the period. Where period is a two dates in the ISO date time format")
    fun getUserYearlyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime,
        @RequestParam(name = "userId", required = true) userId: Long
    ): ResponseEntity<Response<List<MonthStudyStatistic>>> {
        val result = userMonthStatisticService.getStatisticForPeriod(from, to, userId)
        return ResponseEntity.ok().body(Response(data = result))
    }
}
