package com.epam.brn.controller

import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.ExerciseDtoWrapper
import com.epam.brn.service.ExerciseService
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Collections

@RestController
@RequestMapping(BrnPath.EXERCISES)
@Api(value = BrnPath.EXERCISES, description = "Contains actions over exercises")
class ExerciseController(@Autowired val exerciseService: ExerciseService) {

    @GetMapping
    fun getExercisesByUserID(
        @RequestParam(value = "userID") userID: Long
    ): ExerciseDtoWrapper {
        return ExerciseDtoWrapper(data = exerciseService.findDoneExercises(userID))
    }

    @RequestMapping(value = ["/{exerciseID}"], method = [RequestMethod.GET])
    fun getExercisesByID(
        @PathVariable("exerciseID") exerciseID: Long
    ): ExerciseDtoWrapper {
        return ExerciseDtoWrapper(data = Collections.singletonList(exerciseService.findExercisesByID(exerciseID)))
    }
}