package com.epam.brn.controller

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.service.ExerciseService
import com.epam.brn.constant.BrnPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.EXERCISE)
class ExerciseController(@Autowired val exerciseService: ExerciseService) {

    @GetMapping
    fun getExercises(
        @RequestParam(value = "name", defaultValue = "0") name: String,
        @RequestParam(value = "userId", defaultValue = "0") userId: String
    ): List<ExerciseDto> {
        return exerciseService.findExercises(name)
    }
}