package com.epam.brn.controller

import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.dto.statistics.DayStudyStatistics
import com.epam.brn.dto.statistics.MonthStudyStatistics
import com.epam.brn.dto.statistics.UserDailyDetailStatisticsDto
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.RoleService
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.statistics.UserPeriodStatisticsService
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
@Tag(name = "Statistics", description = "Contains actions over user statistics details")
@RolesAllowed(BrnRole.USER)
class UserStatisticControllerV2(
    private val userDayStatisticService: UserPeriodStatisticsService<DayStudyStatistics>,
    private val userMonthStatisticService: UserPeriodStatisticsService<MonthStudyStatistics>,
    private val historyService: StudyHistoryService,
    private val roleService: RoleService,
) {
    @GetMapping("/study/year")
    @Operation(summary = "Get user's yearly statistics for the period. Where period is a two dates in the ISO date time format")
    fun getUserYearlyStatistics(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime,
        @RequestParam(name = "userId") userId: Long?,
    ): ResponseEntity<BrnResponse<List<MonthStudyStatistics>>> {
        val result =
            if (userId != null && roleService.isCurrentUserAdmin()) {
                userMonthStatisticService.getStatisticsForPeriod(from, to, userId)
            } else {
                userMonthStatisticService.getStatisticsForPeriod(from, to)
            }
        return ResponseEntity.ok().body(BrnResponse(data = result))
    }

    @GetMapping("/study/week")
    @Operation(summary = "Get user's weekly statistics for the period. Where period is a two dates in the ISO date time format")
    fun getUserWeeklyStatistics(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime,
        @RequestParam(name = "userId") userId: Long?,
    ): ResponseEntity<BrnResponse<List<DayStudyStatistics>>> {
        val result =
            if (userId != null && roleService.isCurrentUserAdmin()) {
                userDayStatisticService.getStatisticsForPeriod(from, to, userId)
            } else {
                userDayStatisticService.getStatisticsForPeriod(from, to)
            }
        return ResponseEntity.ok().body(BrnResponse(data = result))
    }

    @GetMapping("/study/day")
    @Operation(summary = "Get user's details daily statistics for the day. Where day is a date in the ISO date time format")
    fun getUserDailyDetailsStatistics(
        @RequestParam(name = "day", required = true) day: LocalDateTime,
        @RequestParam(name = "userId") userId: Long?,
    ): ResponseEntity<BrnResponse<List<UserDailyDetailStatisticsDto>>> {
        val result =
            if (userId != null && roleService.isCurrentUserAdmin()) {
                historyService.getUserDailyStatistics(day, userId)
            } else {
                historyService.getUserDailyStatistics(day = day)
            }
        return ResponseEntity.ok().body(BrnResponse(data = result))
    }
}
