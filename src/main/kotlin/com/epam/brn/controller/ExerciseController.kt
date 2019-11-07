package com.epam.brn.controller

import com.epam.brn.constant.BrnParams.EXERCISE_ID
import com.epam.brn.constant.BrnParams.SERIES_ID
import com.epam.brn.constant.BrnParams.USER_ID
import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.service.ExerciseService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.EXERCISES)
@Api(value = BrnPath.EXERCISES, description = "Contains actions over exercises")
class ExerciseController(@Autowired val exerciseService: ExerciseService) {

    @GetMapping
    @ApiOperation("Get done exercises for user")
    fun getExercises(
        @RequestParam(value = USER_ID, required = true) userId: Long,
        @RequestParam(value = SERIES_ID, required = true) seriesId: Long
    ): ResponseEntity<BaseResponseDto> {
            return ResponseEntity.ok()
                .body(BaseResponseDto(data = exerciseService.findExercisesByUserIdAndSeries(userId, seriesId)))
    }

    @GetMapping(value = ["/{$EXERCISE_ID}"])
    @ApiOperation("Get exercise by id")
    fun getExercisesByID(
        @PathVariable(EXERCISE_ID) exerciseId: Long
    ): ResponseEntity<BaseResponseDto> {
        return ResponseEntity.ok().body(BaseResponseDto(data = listOf(exerciseService.findExerciseById(exerciseId))))
    }
}