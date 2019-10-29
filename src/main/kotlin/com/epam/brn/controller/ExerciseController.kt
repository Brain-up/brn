package com.epam.brn.controller

import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.ExerciseDto
import com.epam.brn.service.ExerciseService
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.EXERCISES)
@Api(value = BrnPath.EXERCISES, description = "Contains actions over exercises")
class ExerciseController(@Autowired val exerciseService: ExerciseService) {

    @GetMapping
    fun getExercisesByName(
        @RequestParam(value = "name", required = true) name: String
    ): List<ExerciseDto> {
        return exerciseService.findExercises(name)
    }

    @RequestMapping(value = ["/user/{userID}"], method = [RequestMethod.GET])
    fun getDoneExercises(
        @PathVariable("userID") userID: Long
    ): List<ExerciseDto> {
        return exerciseService.findDoneExercises(userID)
    }
}