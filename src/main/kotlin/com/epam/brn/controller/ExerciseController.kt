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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exercises")
@Api(value = "/exercises", description = "Contains actions over exercises")
class ExerciseController(@Autowired val exerciseService: ExerciseService) {

    @GetMapping
    @ApiOperation("Get all series exercises for current user with availability calculation if withAvailability = true.")
    fun getExercises(
        @RequestParam(value = "seriesId", required = true) seriesId: Long,
        @RequestParam(value = "withAvailability", required = false, defaultValue = true.toString()) withAvailability: Boolean
    ): ResponseEntity<BaseResponseDto> {
        return ResponseEntity.ok()
            .body(BaseResponseDto(data = exerciseService.findAllExercisesBySeriesForCurrentUser(seriesId, withAvailability)))
    }

    @GetMapping(value = ["/byName"])
    @ApiOperation("Get exercises for current user by exercise name.")
    fun getExercisesByName(
        @RequestParam(value = "name", required = true) name: String
    ): ResponseEntity<BaseResponseDto> {
        return ResponseEntity.ok()
            .body(BaseResponseDto(data = exerciseService.findExercisesByNameForCurrentUser(name)))
    }

    @PostMapping(value = ["/byIds"])
    @ApiOperation("Get available exercise ids for current user by ids which have same name.")
    fun getExercisesByIds(
        @Validated @RequestBody exerciseRequest: ExerciseRequest
    ): ResponseEntity<BaseResponseDto> {
        return ResponseEntity.ok()
            .body(BaseResponseDto(data = exerciseService.getExercisesByIds(exerciseRequest.ids)))
    }

    @GetMapping(value = ["/{exerciseId}"])
    @ApiOperation("Get exercise by id.")
    fun getExercisesByID(
        @PathVariable("exerciseId") exerciseId: Long
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        return ResponseEntity.ok()
            .body(BaseSingleObjectResponseDto(data = exerciseService.findExerciseById(exerciseId)))
    }
}
