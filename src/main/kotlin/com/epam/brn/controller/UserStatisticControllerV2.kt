package com.epam.brn.controller

import com.epam.brn.service.RoleService
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.UserDailyDetailStatisticsDto
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/v2/statistics")
@Tag(name = "Statistics", description = "Contains actions over user statistic details")
@RolesAllowed(BrnRole.USER)
class UserStatisticControllerV2(
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>,
    private val historyService: StudyHistoryService,
    private val roleService: RoleService
) {
    @GetMapping("/study/year")
    @Operation(summary = "Get user's yearly statistic for the period. Where period is a two dates in the ISO date time format")
    fun getUserYearlyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime,
        @RequestParam(name = "userId") userId: Long?
    ): ResponseEntity<BrnResponse<List<MonthStudyStatistic>>> {
        val result = if (userId != null && roleService.isCurrentUserAdmin()) {
            userMonthStatisticService.getStatisticForPeriod(from, to, userId)
        } else {
            userMonthStatisticService.getStatisticForPeriod(from, to)
        }
        return ResponseEntity.ok().body(BrnResponse(data = result))
    }

    @GetMapping("/study/week")
    @Operation(summary = "Get user's weekly statistic for the period. Where period is a two dates in the ISO date time format")
    fun getUserWeeklyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime,
        @RequestParam(name = "userId") userId: Long?
    ): ResponseEntity<BrnResponse<List<DayStudyStatistic>>> {
        val result = if (userId != null && roleService.isCurrentUserAdmin()) {
            userDayStatisticService.getStatisticForPeriod(from, to, userId)
        } else {
            userDayStatisticService.getStatisticForPeriod(from, to)
        }
        return ResponseEntity.ok().body(BrnResponse(data = result))
    }

    @GetMapping("/study/day")
    @Operation(summary = "Get user's details daily statistic for the day. Where day is a date in the ISO date time format")
    fun getUserDailyDetailsStatistics(
        @RequestParam(name = "day", required = true) day: LocalDateTime,
        @RequestParam(name = "userId") userId: Long?
    ): ResponseEntity<BrnResponse<List<UserDailyDetailStatisticsDto>>> {
        val result = if (userId != null && roleService.isCurrentUserAdmin()) {
            historyService.getUserDailyStatistics(day, userId)
        } else {
            historyService.getUserDailyStatistics(day = day)
        }
        return ResponseEntity.ok().body(BrnResponse(data = result))
    }
}
