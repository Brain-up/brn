package com.epam.brn.controller

import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.ExerciseDtoWrapper
import com.epam.brn.exception.InvalidParametersException
import com.epam.brn.service.ExerciseService
import io.swagger.annotations.Api
import org.apache.commons.lang3.ObjectUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Collections

@RestController
@RequestMapping(BrnPath.EXERCISES)
@Api(value = BrnPath.EXERCISES, description = "Contains actions over exercises")
class ExerciseController(@Autowired val exerciseService: ExerciseService) {

    @GetMapping
    fun getExercisesByID(
        @RequestParam(value = "exerciseID", required = false) exerciseID: Long?,
        @RequestParam(value = "userID", required = false) userID: Long?
    ): ExerciseDtoWrapper {
        if (isValidParams(exerciseID, userID)) {
            throw InvalidParametersException("Only one argument is allowed")
        }
        if (ObjectUtils.isNotEmpty(exerciseID)) {
            return getExercisesByID(exerciseID!!)
        }
        return getDoneExercises(userID!!)
    }

    private fun isValidParams(exerciseID: Long?, userID: Long?) =
        ObjectUtils.isNotEmpty(exerciseID).xor(ObjectUtils.isNotEmpty(userID))

    private fun getExercisesByID(
        exerciseID: Long
    ): ExerciseDtoWrapper {
        return ExerciseDtoWrapper(data = Collections.singletonList(exerciseService.findExercisesByID(exerciseID)))
    }

    private fun getDoneExercises(
        userID: Long
    ): ExerciseDtoWrapper {
        return ExerciseDtoWrapper(data = exerciseService.findDoneExercises(userID))
    }
}