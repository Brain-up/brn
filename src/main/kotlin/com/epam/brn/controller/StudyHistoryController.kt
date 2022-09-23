package com.epam.brn.controller

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.response.Response
import com.epam.brn.service.StudyHistoryService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/study-history")
@Api(value = "/study-history", tags = ["Study History"], description = "Contains actions over the results of finished exercise")
@RolesAllowed(RoleConstants.USER)
class StudyHistoryController(
    @Autowired val studyHistoryService: StudyHistoryService,
    @Autowired val authorityService: AuthorityService
) {

    @PostMapping
    @ApiOperation("Save current user's study history")
    fun save(@Validated @RequestBody studyHistoryDto: StudyHistoryDto): ResponseEntity<StudyHistoryDto> {
        return ResponseEntity.ok().body(studyHistoryService.save(studyHistoryDto))
    }

    @GetMapping("/todayTimer")
    @ApiOperation("Get current user's today work time: execution seconds")
    fun getTodayWorkDurationInSeconds(): ResponseEntity<Response<Int>> {
        return ResponseEntity.ok().body(Response(data = studyHistoryService.getTodayTimer()))
    }

    @GetMapping("/monthHistories")
    @ApiOperation("Get current user's month study histories by month and year")
    fun getMonthHistories(
        @RequestParam("month", required = true) month: Int,
        @RequestParam("year", required = true) year: Int,
        @RequestParam("userId") userId: Long?
    ): ResponseEntity<Response> {
        val result = if (userId != null && authorityService.hasAuthority(AuthorityType.ROLE_ADMIN)) {
            studyHistoryService.getMonthHistories(userId, month, year)
        } else {
            studyHistoryService.getMonthHistoriesForCurrentUser(month, year)
        }
        return ResponseEntity.ok().body(Response(data = result))
    }
}
