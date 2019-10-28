package com.epam.brn.controller

import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.SeriesDto
import com.epam.brn.service.SeriesService
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.GROUPS)
@Api(value = "${BrnPath.GROUPS}/groupId/${BrnPath.SERIES}", description = "End point for working with series")
class SeriesController(@Autowired val seriesService: SeriesService) {

    @GetMapping("{groupId}/${BrnPath.SERIES}")
    fun getSeriesForGroup(
        @PathVariable(value = "groupId") groupId: Long,
        @RequestParam(value = "include") include: String
    ): List<SeriesDto> {
        return seriesService.findSeriesForGroup(groupId, include)
    }

    @GetMapping("{groupId}/${BrnPath.SERIES}/{seriesId}")
    fun getSeriesForId(
        @PathVariable(value = "groupId") groupId: Long,
        @PathVariable(value = "seriesId") seriesId: Long
    ): SeriesDto {
        return seriesService.findSeriesForId(seriesId)
    }
}