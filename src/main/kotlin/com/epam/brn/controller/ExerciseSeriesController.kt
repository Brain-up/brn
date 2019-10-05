package com.epam.brn.controller

import com.epam.brn.dto.SeriesDto
import com.epam.brn.service.UserDetailsService
import com.lifescience.brn.constant.BrnPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.SERIES)
class ExerciseSeriesController(@Autowired val userDetailService: UserDetailsService) {

    @GetMapping
    fun getSeries(@RequestParam(value = "groupId", defaultValue = "0") groupId: String): List<SeriesDto> {
        return listOf(
            SeriesDto("1", "распознование слов"),
            SeriesDto("2", "диахоническое слушание"))
    }
}