package com.epam.brn.controller

import com.epam.brn.dto.request.SubGroupChangeRequest
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.dto.request.exercise.ExerciseCreateDto
import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.response.BaseSingleObjectResponse
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.SubGroupService
import com.epam.brn.upload.CsvUploadService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
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
class AdminExerciseFlowController(
    private val studyHistoryService: StudyHistoryService,
    private val exerciseService: ExerciseService,
    private val csvUploadService: CsvUploadService,
    private val resourceService: ResourceService,
    private val subGroupService: SubGroupService,
) {

    @GetMapping("/monthHistories")
    @ApiOperation("Get month user's study histories by month and year")
    fun getMonthHistories(
        @RequestParam("userId", required = true) userId: Long,
        @RequestParam("month", required = true) month: Int,
        @RequestParam("year", required = true) year: Int
    ) = ResponseEntity.ok()
        .body(BaseResponse(data = studyHistoryService.getMonthHistories(userId, month, year)))

    @PostMapping("/loadTasksFile")
    @ApiOperation("Load task file to series")
    fun loadExercises(
        @RequestParam(value = "seriesId") seriesId: Long,
        @RequestParam(value = "taskFile") file: MultipartFile
    ): ResponseEntity<BaseResponse> {
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
    ): ResponseEntity<BaseResponse> =
        ResponseEntity.ok()
            .body(BaseResponse(data = exerciseService.findExercisesWithTasksBySubGroup(subGroupId)))

    @PatchMapping("/resources/{id}")
    @ApiOperation("Update resource description by resource id")
    fun updateResourceDescription(
        @PathVariable(value = "id") id: Long,
        @RequestBody @Validated request: UpdateResourceDescriptionRequest
    ): ResponseEntity<BaseSingleObjectResponse> =
        ResponseEntity.ok()
            .body(BaseSingleObjectResponse(data = resourceService.updateDescription(id, request.description!!)))

    @PostMapping("/subgroup")
    @ApiOperation("Add new subgroup for existing series")
    fun addSubGroupToSeries(
        @ApiParam(name = "seriesId", type = "Long", value = "ID of existed series", example = "1")
        @RequestParam(value = "seriesId") seriesId: Long,
        @Valid @RequestBody subGroupRequest: SubGroupRequest
    ): ResponseEntity<BaseSingleObjectResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseSingleObjectResponse(data = subGroupService.addSubGroupToSeries(subGroupRequest, seriesId)))

    @PatchMapping("/subgroups/{subGroupId}")
    @ApiOperation("Update subgroup by id")
    fun updateSubGroupById(
        @PathVariable(value = "subGroupId") subGroupId: Long,
        @RequestBody subGroup: SubGroupChangeRequest
    ): ResponseEntity<BaseSingleObjectResponse> =
        ResponseEntity.ok(BaseSingleObjectResponse(data = subGroupService.updateSubGroupById(subGroupId, subGroup)))

    @PostMapping("/create/exercise")
    @ApiOperation("Create new exercise for exist subgroup")
    fun createExercise(
        @ApiParam(value = "Exercise data", required = true)
        @Valid @RequestBody exerciseCreateDto: ExerciseCreateDto
    ): ResponseEntity<BaseSingleObjectResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseSingleObjectResponse(data = exerciseService.createExercise(exerciseCreateDto)))
}
