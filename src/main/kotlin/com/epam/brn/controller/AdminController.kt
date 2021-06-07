package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.upload.CsvUploadService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@RestController
@RequestMapping("/admin")
@Api(value = "/admin", description = "Contains actions for admin")
class AdminController(
    private val studyHistoryService: StudyHistoryService,
    private val userAccountService: UserAccountService,
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>,
    private val exerciseService: ExerciseService,
    private val csvUploadService: CsvUploadService,
    private val resourceService: ResourceService
) {

    @GetMapping("/users")
    @ApiOperation("Get users")
    fun getUsers(
        @RequestParam("withAnalytics", defaultValue = "false") withAnalytics: Boolean,
        @PageableDefault pageable: Pageable,
    ): ResponseEntity<Any> {
        val users = if (withAnalytics) userAccountService.getUsersWithAnalytics(pageable)
        else userAccountService.getUsers(pageable)
        return ResponseEntity.ok().body(BaseResponseDto(data = users))
    }

    @GetMapping("/histories")
    @ApiOperation("Get user's study histories for period")
    fun getHistories(
        @RequestParam("userId", required = true) userId: Long,
        @RequestParam("from", required = true) from: LocalDate,
        @RequestParam("to", required = true) to: LocalDate
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
        @RequestParam(name = "from", required = true) from: LocalDate,
        @RequestParam(name = "to", required = true) to: LocalDate,
        @RequestParam(name = "userId", required = true) userId: Long
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val result = userDayStatisticService.getStatisticForPeriod(from, to, userId)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = result))
    }

    @GetMapping("/study/year")
    @ApiOperation("Get user's yearly statistic for the period")
    fun getUserYearlyStatistic(
        @RequestParam(name = "from", required = true) from: LocalDate,
        @RequestParam(name = "to", required = true) to: LocalDate,
        @RequestParam(name = "userId", required = true) userId: Long
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val result = userMonthStatisticService.getStatisticForPeriod(from, to, userId)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = result))
    }

    @PostMapping("/loadTasksFile")
    fun loadExercises(
        @RequestParam(value = "seriesId") seriesId: Long,
        @RequestParam(value = "taskFile") file: MultipartFile
    ): ResponseEntity<BaseResponseDto> {
        csvUploadService.loadExercises(seriesId, file)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @GetMapping("/exercises")
    @ApiOperation("Get subGroup exercises with tasks.")
    fun getExercisesBySubGroup(
        @RequestParam(
            value = "subGroupId",
            required = true
        ) subGroupId: Long
    ): ResponseEntity<BaseResponseDto> =
        ResponseEntity.ok()
            .body(BaseResponseDto(data = exerciseService.findExercisesWithTasksBySubGroup(subGroupId)))

    @PatchMapping("/resources/{id}")
    @ApiOperation("Update resource description by resource id.")
    fun updateResourceDescription(
        @PathVariable(value = "id") id: Long,
        @RequestBody @Validated request: UpdateResourceDescriptionRequest
    ): ResponseEntity<BaseSingleObjectResponseDto> =
        ResponseEntity.ok()
            .body(BaseSingleObjectResponseDto(data = resourceService.updateDescription(id, request.description!!)))
}
