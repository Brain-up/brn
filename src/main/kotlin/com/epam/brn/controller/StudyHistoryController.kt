package com.epam.brn.controller

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.response.BaseSingleObjectResponse
import com.epam.brn.service.StudyHistoryService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import java.time.LocalDateTime

@RestController
@RequestMapping("/study-history")
@Api(value = "/study-history", description = "Contains actions over the results of finished exercise")
class StudyHistoryController(@Autowired val studyHistoryService: StudyHistoryService) {

    @PostMapping
    @ApiOperation("Save current user's study history")
    fun save(@Validated @RequestBody studyHistoryDto: StudyHistoryDto): ResponseEntity<StudyHistoryDto> {
        return ResponseEntity.ok().body(studyHistoryService.save(studyHistoryDto))
    }

    @GetMapping("/todayTimer")
    @ApiOperation("Get current user's today work time: execution seconds")
    fun getTodayWorkDurationInSeconds(): ResponseEntity<BaseSingleObjectResponse> {
        return ResponseEntity.ok().body(BaseSingleObjectResponse(data = studyHistoryService.getTodayTimer()))
    }

    @GetMapping("/monthHistories")
    @ApiOperation("Get current user's month study histories by month and year")
    fun getMonthHistories(
        @RequestParam("month", required = true) month: Int,
        @RequestParam("year", required = true) year: Int
    ) = ResponseEntity.ok()
        .body(BaseResponse(data = studyHistoryService.getMonthHistoriesForCurrentUser(month, year)))

    @GetMapping("/histories")
    @ApiOperation("Get current user's study histories for period from <= startTime < to. Where from and to are dates in ISO format")
    fun getHistories(
        @RequestParam("from", required = true) from: LocalDateTime,
        @RequestParam("to", required = true) to: LocalDateTime
    ) = ResponseEntity.ok()
        .body(BaseResponse(data = studyHistoryService.getHistoriesForCurrentUser(from, to)))

    @GetMapping("/user/{userId}/has/statistics")
    @ApiOperation("Check if user has statistics")
    fun isUserHasStatistics(
        @PathVariable("userId") userId: Long
    ) = ResponseEntity.ok()
        .body(BaseSingleObjectResponse(data = studyHistoryService.isUserHasStatistics(userId)))
}
