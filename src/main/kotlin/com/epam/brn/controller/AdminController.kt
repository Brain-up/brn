package com.epam.brn.controller

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.dto.request.exercise.ExerciseCreateDto
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.SubGroupService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.upload.CsvUploadService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
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
import java.time.LocalDateTime
import java.time.LocalTime
import javax.validation.Valid

@RestController
@RequestMapping("/admin")
@Api(value = "/admin", description = "Contains actions for admin")
class AdminController(
    private val studyHistoryService: StudyHistoryService,
    private val userAccountService: UserAccountService,
    private val userAnalyticsService: UserAnalyticsService,
    private val userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>,
    private val userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>,
    private val exerciseService: ExerciseService,
    private val csvUploadService: CsvUploadService,
    private val resourceService: ResourceService,
    private val subGroupService: SubGroupService,
    private val authorityService: AuthorityService
) {

    @GetMapping("/users")
    @ApiOperation("Get all users")
    fun getUsers(
        @RequestParam("withAnalytics", defaultValue = "false") withAnalytics: Boolean,
        @RequestParam("role", defaultValue = "ROLE_USER") role: String,
        @PageableDefault pageable: Pageable,
    ): ResponseEntity<Any> {
        val users = if (withAnalytics) userAnalyticsService.getUsersWithAnalytics(pageable, role)
        else userAccountService.getUsers(pageable, role)
        return ResponseEntity.ok().body(BaseResponseDto(data = users))
    }

    @GetMapping("/histories")
    @ApiOperation("Get user's study histories for period from <= startTime < to. Where period is a two dates in the format yyyy-MM-dd")
    @Deprecated(
        message = "Use the method with LocalDateTime as the dates type instead",
        replaceWith = ReplaceWith("getHistories(from, to)", imports = ["com.epam.brn.controller.AdminControllerV2"])
    )
    fun getHistories(
        @RequestParam("userId", required = true) userId: Long,
        @RequestParam("from", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") from: LocalDate,
        @RequestParam("to", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") to: LocalDate
    ) = ResponseEntity.ok()
        .body(BaseResponseDto(data = studyHistoryService.getHistories(userId, from, to)))

    @GetMapping("/monthHistories")
    @ApiOperation("Get month user's study histories by month and yea")
    fun getMonthHistories(
        @RequestParam("userId", required = true) userId: Long,
        @RequestParam("month", required = true) month: Int,
        @RequestParam("year", required = true) year: Int
    ) = ResponseEntity.ok()
        .body(BaseResponseDto(data = studyHistoryService.getMonthHistories(userId, month, year)))

    @GetMapping("/study/week")
    @ApiOperation("Get user's weekly statistic for the period. Where period is a two dates in the format yyyy-MM-dd")
    @Deprecated(
        message = "Use the method with LocalDateTime as the dates type instead",
        replaceWith = ReplaceWith("getUserWeeklyStatistic(from, to)", imports = ["com.epam.brn.controller.AdminControllerV2"])
    )
    fun getUserWeeklyStatistic(
        @RequestParam(name = "from", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") from: LocalDate,
        @RequestParam(name = "to", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") to: LocalDate,
        @RequestParam(name = "userId", required = true) userId: Long
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val tempFrom = LocalDateTime.of(from, LocalTime.MIN)
        val tempTo = LocalDateTime.of(to, LocalTime.MAX)
        val result = userDayStatisticService.getStatisticForPeriod(tempFrom, tempTo, userId)
        val response = result.map {
            it.toDto()
        }
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = response))
    }

    @GetMapping("/study/year")
    @ApiOperation("Get user's yearly statistic for the period. Where period is a two dates in the format yyyy-MM-dd")
    @Deprecated(
        message = "Use the method with LocalDateTime as the dates type instead",
        replaceWith = ReplaceWith("getUserYearlyStatistic(from, to)", imports = ["com.epam.brn.controller.AdminControllerV2"])
    )
    fun getUserYearlyStatistic(
        @RequestParam(name = "from", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") from: LocalDate,
        @RequestParam(name = "to", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") to: LocalDate,
        @RequestParam(name = "userId", required = true) userId: Long
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        val tempFrom = LocalDateTime.of(from, LocalTime.MIN)
        val tempTo = LocalDateTime.of(to, LocalTime.MAX)
        val result = userMonthStatisticService.getStatisticForPeriod(tempFrom, tempTo, userId)
        val response = result.map {
            it.toDto()
        }
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = response))
    }

    @PostMapping("/loadTasksFile")
    @ApiOperation("Load task file to series")
    fun loadExercises(
        @RequestParam(value = "seriesId") seriesId: Long,
        @RequestParam(value = "taskFile") file: MultipartFile
    ): ResponseEntity<BaseResponseDto> {
        csvUploadService.loadExercises(seriesId, file)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @GetMapping("/exercises")
    @ApiOperation("Get exercises for subgroup with tasks")
    fun getExercisesBySubGroup(
        @RequestParam(
            value = "subGroupId",
            required = true
        ) subGroupId: Long
    ): ResponseEntity<BaseResponseDto> =
        ResponseEntity.ok()
            .body(BaseResponseDto(data = exerciseService.findExercisesWithTasksBySubGroup(subGroupId)))

    @PatchMapping("/resources/{id}")
    @ApiOperation("Update resource description by resource id")
    fun updateResourceDescription(
        @PathVariable(value = "id") id: Long,
        @RequestBody @Validated request: UpdateResourceDescriptionRequest
    ): ResponseEntity<BaseSingleObjectResponseDto> =
        ResponseEntity.ok()
            .body(BaseSingleObjectResponseDto(data = resourceService.updateDescription(id, request.description!!)))

    @PostMapping("/subgroup")
    @ApiOperation("Add new subgroup for existing series")
    fun addSubGroupToSeries(
        @ApiParam(name = "seriesId", type = "Long", value = "ID of existed series", example = "1")
        @RequestParam(value = "seriesId") seriesId: Long,
        @Valid @RequestBody subGroupRequest: SubGroupRequest
    ): ResponseEntity<BaseSingleObjectResponseDto> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseSingleObjectResponseDto(data = subGroupService.addSubGroupToSeries(subGroupRequest, seriesId)))

    @GetMapping("/roles")
    @ApiOperation("Get all roles")
    fun getRoles(): ResponseEntity<BaseResponseDto> {
        val authorities = authorityService.findAll()
        return ResponseEntity.ok().body(BaseResponseDto(data = authorities))
    }

    @PostMapping("/create/exercise")
    @ApiOperation("Create new exercise for exist subgroup")
    fun createExercise(
        @ApiParam(value = "Exercise data", required = true)
        @Valid @RequestBody exerciseCreateDto: ExerciseCreateDto
    ): ResponseEntity<BaseSingleObjectResponseDto> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseSingleObjectResponseDto(data = exerciseService.createExercise(exerciseCreateDto)))
}
