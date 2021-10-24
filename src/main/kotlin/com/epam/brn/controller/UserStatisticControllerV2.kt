package com.epam.brn.controller

import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.service.statistic.UserPeriodStatisticService
import io.swagger.annotations.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v2/statistics")
@Api(value = "/statistics", description = "User statistic details")
class UserStatisticControllerV2(
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>
) {

    @GetMapping(value = ["/study/week"])
    fun getUserWeeklyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val result = userDayStatisticService.getStatisticForPeriod(from, to)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = result))
    }

    @GetMapping(value = ["/study/year"])
    fun getUserYearlyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val result = userMonthStatisticService.getStatisticForPeriod(from, to)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = result))
    }
}
