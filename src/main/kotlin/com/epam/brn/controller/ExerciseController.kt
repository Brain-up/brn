package com.epam.brn.controller

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.service.UserDetailsService
import com.lifescience.brn.constant.BrnPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.EXERCISE)
class ExerciseController(@Autowired val userDetailService: UserDetailsService) {

    @GetMapping
    fun getExercises(@RequestParam(value = "seriesId", defaultValue = "0") groupId: String): List<ExerciseDto> {
        return listOf(
            ExerciseDto("1", "однослоговые слова"),
            ExerciseDto("2", "двуслоговые слова слова"),
            ExerciseDto("3", "сложные слова слова"))
    }
}