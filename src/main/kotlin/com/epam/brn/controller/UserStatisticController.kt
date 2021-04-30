package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.response.SubGroupStatisticDto
import com.epam.brn.dto.statistic.WeekStudyStatistic
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.service.statistic.UserStatisticService
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 *@author Nikolai Lazarev
 */
@RestController
@RequestMapping("/statistics")
@Api(value = "/statistics", description = "User statistic details")
class UserStatisticController(
    @Autowired
    val userStatisticService: UserStatisticService<SubGroupStatisticDto>,
    val userPeriodStatisticService: UserPeriodStatisticService<WeekStudyStatistic>
) {

    @GetMapping("/subgroups")
    fun getUserSubGroupStatistic(
        @RequestParam(value = "ids", required = true) ids: List<Long>
    ): ResponseEntity<BaseResponseDto> {
        val userStatistic = userStatisticService.getSubGroupStatistic(ids)
        return ResponseEntity.ok().body(BaseResponseDto(data = userStatistic))
    }

    @GetMapping("/study")
    fun getUserStudyStatistic(
        @RequestParam(name = "from", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") from: LocalDate,
        @RequestParam(name = "to", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") to: LocalDate
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val result = userPeriodStatisticService.getStatisticForPeriod(from, to)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = result))
    }
}
