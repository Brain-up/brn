package com.epam.brn.controller

import com.epam.brn.service.cloud.CloudService
import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.response.BaseSingleObjectResponse
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@RestController
@RequestMapping("/v2/admin")
@Api(value = "/v2/admin", description = "Contains actions for admin")
class AdminControllerV2(
    private val studyHistoryService: StudyHistoryService,
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>,
    private val cloudService: CloudService,
) {

    @GetMapping("/histories")
    @ApiOperation("Get user's study histories for period from <= startTime <= to where startTime is a date in ISO date time format")
    fun getHistories(
        @RequestParam("userId", required = true) userId: Long,
        @RequestParam("from", required = true) from: LocalDateTime,
        @RequestParam("to", required = true) to: LocalDateTime
    ) = ResponseEntity.ok()
        .body(BaseResponse(data = studyHistoryService.getHistories(userId, from, to)))

    @GetMapping("/study/week")
    @ApiOperation("Get user's weekly statistic for the period. Where period is a two dates in the ISO date time format")
    fun getUserWeeklyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime,
        @RequestParam(name = "userId", required = true) userId: Long
    ): ResponseEntity<BaseSingleObjectResponse> {
        val result = userDayStatisticService.getStatisticForPeriod(from, to, userId)
        return ResponseEntity.ok().body(BaseSingleObjectResponse(data = result))
    }

    @GetMapping("/study/year")
    @ApiOperation("Get user's yearly statistic for the period. Where period is a two dates in the ISO date time format")
    fun getUserYearlyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDateTime,
        @RequestParam(name = "to", required = true) to: LocalDateTime,
        @RequestParam(name = "userId", required = true) userId: Long
    ): ResponseEntity<BaseSingleObjectResponse> {
        val result = userMonthStatisticService.getStatisticForPeriod(from, to, userId)
        return ResponseEntity.ok().body(BaseSingleObjectResponse(data = result))
    }

    @PostMapping("/upload")
    @ApiOperation("Load verified files to cloud storage")
    fun upload(
        @RequestParam(value = "path") path: String,
        @RequestParam(value = "filename", required = false) fileName: String?,
        @RequestParam(value = "file") multipartFile: MultipartFile
    ): ResponseEntity<BaseResponse> {
        cloudService.uploadFile(path, fileName ?: multipartFile.name, multipartFile.inputStream)
        return ResponseEntity(HttpStatus.CREATED)
    }
}
