package com.epam.brn.controller

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.request.ExerciseRequest
import com.epam.brn.dto.request.exercise.ExerciseCreateDto
import com.epam.brn.dto.response.ExerciseWithTasksResponse
import com.epam.brn.dto.response.Response
import com.epam.brn.enums.RoleConstants
import com.epam.brn.service.ExerciseService
import com.epam.brn.upload.CsvUploadService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import javax.annotation.security.RolesAllowed
import javax.validation.Valid

@RestController
@RequestMapping("/exercises")
@Api(value = "/exercises", tags = ["Exercises"], description = "Contains actions over exercises")
@RolesAllowed(RoleConstants.USER)
class ExerciseController(
    @Autowired val exerciseService: ExerciseService,
    @Autowired val csvUploadService: CsvUploadService
) {

    @PostMapping
    @ApiOperation("Create new exercise for existing subgroup")
    @RolesAllowed(RoleConstants.ADMIN)
    fun createExercise(
        @ApiParam(value = "Exercise data", required = true)
        @Valid @RequestBody exerciseCreateDto: ExerciseCreateDto
    ): ResponseEntity<Response<ExerciseDto>> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(data = exerciseService.createExercise(exerciseCreateDto)))

    @GetMapping("/search")
    @ApiOperation("Get exercises for subgroup with tasks")
    @RolesAllowed(RoleConstants.ADMIN)
    fun searchExercisesBySubGroup(
        @RequestParam(
            value = "subGroupId",
            required = true
        ) subGroupId: Long
    ): ResponseEntity<Response<List<ExerciseWithTasksResponse>>> =
        ResponseEntity.ok()
            .body(Response(data = exerciseService.findExercisesWithTasksBySubGroup(subGroupId)))

    @GetMapping
    @ApiOperation("Get exercises for subgroup and current user with availability calculation")
    fun getExercisesBySubGroup(@RequestParam(value = "subGroupId", required = true) subGroupId: Long): ResponseEntity<Response<List<ExerciseDto>>> {
        return ResponseEntity.ok()
            .body(Response(data = exerciseService.findExercisesBySubGroupForCurrentUser(subGroupId)))
    }

    @GetMapping(value = ["/{exerciseId}"])
    @ApiOperation("Get exercise by id")
    fun getExercisesByID(
        @PathVariable("exerciseId") exerciseId: Long
    ): ResponseEntity<Response<ExerciseDto>> {
        return ResponseEntity.ok()
            .body(Response(data = exerciseService.findExerciseById(exerciseId)))
    }

    @PostMapping(value = ["/byIds"])
    @ApiOperation("Get available exercise ids for current user by input ids which have same subgroup")
    fun getExercisesByIds(
        @Validated @RequestBody exerciseRequest: ExerciseRequest
    ): ResponseEntity<Response<List<Long>>> {
        return ResponseEntity.ok()
            .body(Response(data = exerciseService.getAvailableExerciseIds(exerciseRequest.ids)))
    }

    @PutMapping(value = ["/{exerciseId}/active/{active}"])
    @ApiOperation("Update active status of the exercise")
    fun updateExerciseStatus(@PathVariable("exerciseId") exerciseId: Long, @PathVariable("active") active: Boolean) {
        exerciseService.updateActiveStatus(exerciseId, active)
    }

    @PostMapping("/loadTasksFile")
    @ApiOperation("Load task file to series")
    @RolesAllowed(RoleConstants.ADMIN)
    fun loadExercises(
        @RequestParam(value = "seriesId") seriesId: Long,
        @RequestParam(value = "taskFile") file: MultipartFile
    ): ResponseEntity<Response<Any>> {
        csvUploadService.loadExercises(seriesId, file)
        return ResponseEntity(HttpStatus.CREATED)
    }
}
