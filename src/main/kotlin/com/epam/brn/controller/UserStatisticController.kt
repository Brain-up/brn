package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.response.SubGroupStatisticDto
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.service.statistic.UserStatisticService
import io.swagger.annotations.Api
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
@Api(value = "/statistics", description = "User statistic details")
class UserStatisticController(
    private val userStatisticService: UserStatisticService<SubGroupStatisticDto>,
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>
) {

    @GetMapping("/subgroups")
    fun getUserSubGroupStatistic(
        @RequestParam(value = "ids", required = true) ids: List<Long>
    ): ResponseEntity<BaseResponseDto> {
        val userStatistic = userStatisticService.getSubGroupStatistic(ids)
        return ResponseEntity.ok().body(BaseResponseDto(data = userStatistic))
    }

    @GetMapping("/study/week")
    @Deprecated(message = "Use the same method with LocalDateTime as the dates type instead")
    fun getUserWeeklyStatistic(
        @RequestParam(name = "from", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") from: LocalDate,
        @RequestParam(name = "to", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") to: LocalDate
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val tempFrom = LocalDateTime.of(from, LocalTime.MIN)
        val tempTo = LocalDateTime.of(to, LocalTime.MAX)
        val result = userDayStatisticService.getStatisticForPeriod(tempFrom, tempTo)
        val response = result.map {
            it.toDto()
        }
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = response))
    }

    @GetMapping("/study/year")
    @Deprecated(message = "Use the same method with LocalDateTime as the dates type instead")
    fun getUserYearlyStatistic(
        @RequestParam(name = "from", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") from: LocalDate,
        @RequestParam(name = "to", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") to: LocalDate
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val tempFrom = LocalDateTime.of(from, LocalTime.MIN)
        val tempTo = LocalDateTime.of(to, LocalTime.MAX)
        val result = userMonthStatisticService.getStatisticForPeriod(tempFrom, tempTo)
        val response = result.map {
            it.toDto()
        }
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = response))
    }

    @GetMapping(value = ["/study/week"], params = ["version=2"])
    fun getUserWeeklyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val result = userDayStatisticService.getStatisticForPeriod(from, to)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = result))
    }

    @GetMapping(value = ["/study/year"], params = ["version=2"])
    fun getUserYearlyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val result = userMonthStatisticService.getStatisticForPeriod(from, to)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = result))
    }
}
