package com.epam.brn.controller

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.ResourceDto
import com.epam.brn.dto.response.Response
import com.epam.brn.dto.request.SubGroupChangeRequest
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.dto.request.exercise.ExerciseCreateDto
import com.epam.brn.dto.response.AuthorityResponse
import com.epam.brn.dto.response.ExerciseWithTasksResponse
import com.epam.brn.dto.response.SubGroupResponse
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.SubGroupService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import com.epam.brn.upload.CsvUploadService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
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
import javax.validation.Valid

@RestController
@RequestMapping("/admin")
@Api(value = "/admin", description = "Contains actions for admin")
class AdminController(
    private val studyHistoryService: StudyHistoryService,
    private val userAccountService: UserAccountService,
    private val userAnalyticsService: UserAnalyticsService,
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
    ): ResponseEntity<Response<List<Any>>> {
        val users = if (withAnalytics) userAnalyticsService.getUsersWithAnalytics(pageable, role)
        else userAccountService.getUsers(pageable, role)
        return ResponseEntity.ok().body(Response(data = users))
    }

    @GetMapping("/monthHistories")
    @ApiOperation("Get month user's study histories by month and year")
    fun getMonthHistories(
        @RequestParam("userId", required = true) userId: Long,
        @RequestParam("month", required = true) month: Int,
        @RequestParam("year", required = true) year: Int
    ) = ResponseEntity.ok()
        .body(Response(data = studyHistoryService.getMonthHistories(userId, month, year)))

    @PostMapping("/loadTasksFile")
    @ApiOperation("Load task file to series")
    fun loadExercises(
        @RequestParam(value = "seriesId") seriesId: Long,
        @RequestParam(value = "taskFile") file: MultipartFile
    ): ResponseEntity<Any> {
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
    ): ResponseEntity<Response<List<ExerciseWithTasksResponse>>> =
        ResponseEntity.ok()
            .body(Response(data = exerciseService.findExercisesWithTasksBySubGroup(subGroupId)))

    @PatchMapping("/resources/{id}")
    @ApiOperation("Update resource description by resource id")
    fun updateResourceDescription(
        @PathVariable(value = "id") id: Long,
        @RequestBody @Validated request: UpdateResourceDescriptionRequest
    ): ResponseEntity<Response<ResourceDto>> =
        ResponseEntity.ok()
            .body(Response(data = resourceService.updateDescription(id, request.description!!)))

    @PostMapping("/subgroup")
    @ApiOperation("Add new subgroup for existing series")
    fun addSubGroupToSeries(
        @ApiParam(name = "seriesId", type = "Long", value = "ID of existed series", example = "1")
        @RequestParam(value = "seriesId") seriesId: Long,
        @Valid @RequestBody subGroupRequest: SubGroupRequest
    ): ResponseEntity<Response<SubGroupResponse>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(data = subGroupService.addSubGroupToSeries(subGroupRequest, seriesId)))

    @PatchMapping("/subgroups/{subGroupId}")
    @ApiOperation("Update subgroup by id")
    fun updateSubGroupById(
        @PathVariable(value = "subGroupId") subGroupId: Long,
        @RequestBody subGroup: SubGroupChangeRequest
    ): ResponseEntity<Response<SubGroupResponse>> =
        ResponseEntity.ok(Response(data = subGroupService.updateSubGroupById(subGroupId, subGroup)))

    @GetMapping("/roles")
    @ApiOperation("Get all roles")
    fun getRoles(): ResponseEntity<Response<List<AuthorityResponse>>> {
        val authorities = authorityService.findAll()
        return ResponseEntity.ok().body(Response(data = authorities))
    }

    @PostMapping("/create/exercise")
    @ApiOperation("Create new exercise for exist subgroup")
    fun createExercise(
        @ApiParam(value = "Exercise data", required = true)
        @Valid @RequestBody exerciseCreateDto: ExerciseCreateDto
    ): ResponseEntity<Response<ExerciseDto>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(data = exerciseService.createExercise(exerciseCreateDto)))
}
