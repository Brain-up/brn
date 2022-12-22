package com.epam.brn.controller

import com.epam.brn.service.RoleService
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.StudyHistoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(
    name = "Study History",
    description = "Contains actions over the results of finished exercise"
)
@RolesAllowed(BrnRole.USER)
class StudyHistoryController(
    @Autowired val studyHistoryService: StudyHistoryService,
    @Autowired val roleService: RoleService
) {

    @PostMapping
    @Operation(summary = "Save current user's study history")
    fun save(@Validated @RequestBody studyHistoryDto: StudyHistoryDto): ResponseEntity<StudyHistoryDto> {
        return ResponseEntity.ok().body(studyHistoryService.save(studyHistoryDto))
    }

    @GetMapping("/todayTimer")
    @Operation(summary = "Get current user's today work time: execution seconds")
    fun getTodayWorkDurationInSeconds(): ResponseEntity<BrnResponse<Int>> {
        return ResponseEntity.ok().body(BrnResponse(data = studyHistoryService.getTodayTimer()))
    }

    @GetMapping("/monthHistories")
    @Operation(summary = "Get current user's month study histories by month and year")
    fun getMonthHistories(
        @RequestParam("month", required = true) month: Int,
        @RequestParam("year", required = true) year: Int,
        @RequestParam("userId") userId: Long?
    ): ResponseEntity<BrnResponse<List<StudyHistoryDto>>> {
        val result = if (userId != null && roleService.isCurrentUserAdmin()) {
            studyHistoryService.getMonthHistories(userId, month, year)
        } else {
            studyHistoryService.getMonthHistoriesForCurrentUser(month, year)
        }
        return ResponseEntity.ok().body(BrnResponse(data = result))
    }
}
