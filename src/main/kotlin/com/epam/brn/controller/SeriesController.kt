package com.epam.brn.controller

import com.epam.brn.service.SeriesService
import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.SeriesDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.SERIES)
class SeriesController(@Autowired val seriesService: SeriesService) {

    @GetMapping
    fun getSeries(@RequestParam(value = "groupId", defaultValue = "0") groupId: String): List<SeriesDto> {
        return seriesService.findSeries("1")
    }
}