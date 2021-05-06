package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/admin")
@Api(value = "/admin", description = "Contains actions for admin")
class AdminController(
    val studyHistoryService: StudyHistoryService,
    val userAccountService: UserAccountService,
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>
) {

    @GetMapping("/users")
    @ApiOperation("Get users")
    fun getUsers() = ResponseEntity.ok()
        .body(BaseResponseDto(data = userAccountService.getUsers()))

    @GetMapping("/histories")
    @ApiOperation("Get user's study histories for period")
    fun getHistories(
        @RequestParam("userId", required = true) userId: Long,
        @RequestParam("from", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") from: LocalDate,
        @RequestParam("to", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") to: LocalDate
    ) = ResponseEntity.ok()
        .body(BaseResponseDto(data = studyHistoryService.getHistories(userId, from, to)))

    @GetMapping("/monthHistories")
    @ApiOperation("Get month user's study histories")
    fun getMonthHistories(
        @RequestParam("userId", required = true) userId: Long,
        @RequestParam("month", required = true) month: Int,
        @RequestParam("year", required = true) year: Int
    ) = ResponseEntity.ok()
        .body(BaseResponseDto(data = studyHistoryService.getMonthHistories(userId, month, year)))

    @GetMapping("/study/week")
    @ApiOperation("Get user's weekly statistic for the period")
    fun getUserWeeklyStatistic(
        @RequestParam(name = "from", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") from: LocalDate,
        @RequestParam(name = "to", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") to: LocalDate,
        @RequestParam(name = "userId", required = true) userId: Long
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val result = userDayStatisticService.getStatisticForPeriod(from, to, userId)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = result))
    }

    @GetMapping("/study/year")
    @ApiOperation("Get user's yearly statistic for the period")
    fun getUserYearlyStatistic(
        @RequestParam(name = "from", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") from: LocalDate,
        @RequestParam(name = "to", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") to: LocalDate,
        @RequestParam(name = "userId", required = true) userId: Long
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val result = userMonthStatisticService.getStatisticForPeriod(from, to, userId)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = result))
    }
}
