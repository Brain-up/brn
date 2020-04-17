package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.service.SeriesService
import com.epam.brn.upload.CsvUploadService
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/series")
@Api(value = "/series", description = "End points for working with series")
class SeriesController(@Autowired val seriesService: SeriesService, @Autowired val csvUploadService: CsvUploadService) {

    @GetMapping
    fun getSeriesForGroup(@RequestParam(value = "groupId") groupId: Long): ResponseEntity<BaseResponseDto> {
        val listDto = seriesService.findSeriesForGroup(groupId)
        return ok(BaseResponseDto(data = listDto))
    }

    @GetMapping("{seriesId}")
    fun getSeriesForId(@PathVariable(value = "seriesId") seriesId: Long): ResponseEntity<BaseSingleObjectResponseDto> {
        val seriesDto = seriesService.findSeriesDtoForId(seriesId)
        return ok(BaseSingleObjectResponseDto(data = seriesDto))
    }

    @GetMapping("/fileFormat/{seriesId}")
    fun getSampleStringForSeriesFile(
        @PathVariable(value = "seriesId") seriesId: Long
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        return ok(BaseSingleObjectResponseDto(csvUploadService.getSampleStringForSeriesExerciseFile(seriesId)))
    }
}
