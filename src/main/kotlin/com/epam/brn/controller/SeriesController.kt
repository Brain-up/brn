package com.epam.brn.controller

import com.epam.brn.constant.BrnParams.GROUP_ID
import com.epam.brn.constant.BrnParams.SERIES_ID
import com.epam.brn.constant.BrnPath
import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.service.SeriesService
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.SERIES)
@Api(value = "${BrnPath.SERIES}", description = "End points for working with series")
class SeriesController(@Autowired val seriesService: SeriesService) {

    @GetMapping
    fun getSeriesForGroup(@RequestParam(value = GROUP_ID) groupId: Long): ResponseEntity<BaseResponseDto> {
        val listDto = seriesService.findSeriesForGroup(groupId)
        return ResponseEntity.ok().body(BaseResponseDto(data = listDto))
    }

    @GetMapping("{$SERIES_ID}")
    fun getSeriesForId(@PathVariable(value = SERIES_ID) seriesId: Long): ResponseEntity<BaseSingleObjectResponseDto> {
        val seriesDto = seriesService.findSeriesForId(seriesId)
        return ResponseEntity.ok().body(BaseSingleObjectResponseDto(data = seriesDto))
    }
}