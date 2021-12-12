package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.request.ExerciseRequest
import com.epam.brn.service.ExerciseService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
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

@RestController
@RequestMapping("/exercises")
@Api(value = "/exercises", description = "Contains actions over exercises")
class ExerciseController(@Autowired val exerciseService: ExerciseService) {

    @GetMapping
    @ApiOperation("Get exercises for subgroup and current user with availability calculation")
    fun getExercisesBySubGroup(@RequestParam(value = "subGroupId", required = true) subGroupId: Long): ResponseEntity<BaseResponseDto> {
        return ResponseEntity.ok()
            .body(BaseResponseDto(data = exerciseService.findExercisesBySubGroupForCurrentUser(subGroupId)))
    }

    @GetMapping(value = ["/{exerciseId}"])
    @ApiOperation("Get exercise by id")
    fun getExercisesByID(
        @PathVariable("exerciseId") exerciseId: Long
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        return ResponseEntity.ok()
            .body(BaseSingleObjectResponseDto(data = exerciseService.findExerciseById(exerciseId)))
    }

    @PostMapping(value = ["/byIds"])
    @ApiOperation("Get available exercise ids for current user by input ids which have same subgroup")
    fun getExercisesByIds(
        @Validated @RequestBody exerciseRequest: ExerciseRequest
    ): ResponseEntity<BaseResponseDto> {
        return ResponseEntity.ok()
            .body(BaseResponseDto(data = exerciseService.getAvailableExerciseIds(exerciseRequest.ids)))
    }

    @PutMapping(value = ["/{exerciseId}/active/{active}"])
    @ApiOperation("Update active status of the exercise")
    fun updateExerciseStatus(@PathVariable("exerciseId") exerciseId: Long, @PathVariable("active") active: Boolean) {
        exerciseService.updateActiveStatus(exerciseId, active)
    }
}
