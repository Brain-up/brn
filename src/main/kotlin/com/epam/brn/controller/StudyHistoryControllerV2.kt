package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.service.StudyHistoryService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v2/study-history")
@Api(value = "/v2/study-history", description = "Contains actions over the results of finished exercise")
class StudyHistoryControllerV2(
    @Autowired val studyHistoryService: StudyHistoryService
) {

    @GetMapping(value = ["/histories"])
    @ApiOperation("Get current user's study histories for period")
    fun getHistories(
        @RequestParam("from", required = true) from: LocalDateTime,
        @RequestParam("to", required = true) to: LocalDateTime
    ) = ResponseEntity.ok()
        .body(BaseResponseDto(data = studyHistoryService.getHistoriesForCurrentUser(from, to)))
}
