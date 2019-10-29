package com.epam.brn.controller

import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.ExerciseDtoWrapper
import com.epam.brn.service.ExerciseService
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(BrnPath.EXERCISES)
@Api(value = BrnPath.EXERCISES, description = "Contains actions over exercises")
class ExerciseController(@Autowired val exerciseService: ExerciseService) {

    @GetMapping
    fun getExercisesByName(
        @RequestParam(value = "name", required = true) name: String
    ): ExerciseDtoWrapper {
        return ExerciseDtoWrapper(data = exerciseService.findExercisesByNameLike(name))
    }

    @GetMapping
    fun getExercisesByID(
        @RequestParam(value = "userID", required = true) userID: Long
    ): ExerciseDtoWrapper {
        return ExerciseDtoWrapper(data = Collections.singletonList(exerciseService.findExercisesByID(userID)))
    }

    @GetMapping
    fun getDoneExercises(
        @RequestParam(value = "userID", required = true) userID: Long
    ): ExerciseDtoWrapper {
        return ExerciseDtoWrapper(data = exerciseService.findDoneExercises(userID))
    }
}