package com.epam.brn.controller

import com.epam.brn.dto.SeriesDto
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.SeriesService
import com.epam.brn.upload.CsvUploadService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/series")
@Tag(name = "Series", description = "Contains actions over series")
@RolesAllowed(BrnRole.USER)
class SeriesController(@Autowired val seriesService: SeriesService, @Autowired val csvUploadService: CsvUploadService) {

    @GetMapping
    @Operation(summary = "Get all series for group")
    fun getSeriesForGroup(@RequestParam(value = "groupId") groupId: Long): ResponseEntity<BrnResponse<List<SeriesDto>>> {
        val listDto = seriesService.findSeriesForGroup(groupId)
        return ok(BrnResponse(data = listDto))
    }

    @GetMapping("{seriesId}")
    @Operation(summary = "Get series for id")
    fun getSeriesForId(@PathVariable(value = "seriesId") seriesId: Long): ResponseEntity<BrnResponse<SeriesDto>> {
        val seriesDto = seriesService.findSeriesDtoForId(seriesId)
        return ok(BrnResponse(data = seriesDto))
    }

    @GetMapping("/fileFormat/{seriesId}")
    @Operation(summary = "Get series file format by series id")
    fun getSampleStringForSeriesFile(
        @PathVariable(value = "seriesId") seriesId: Long
    ): ResponseEntity<BrnResponse<String>> {
        return ok(BrnResponse(csvUploadService.getSampleStringForSeriesExerciseFile(seriesId)))
    }
}
