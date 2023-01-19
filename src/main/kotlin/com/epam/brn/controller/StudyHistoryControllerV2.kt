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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/v2/study-history")
@Tag(name = "Study History", description = "Contains actions over the results of finished exercise")
@RolesAllowed(BrnRole.USER)
class StudyHistoryControllerV2(
    @Autowired val studyHistoryService: StudyHistoryService,
    @Autowired val roleService: RoleService
) {
    @GetMapping("/histories")
    @Operation(summary = "Get user's study histories for period from <= startTime <= to where startTime is a date in ISO date time format")
    fun getHistories(
        @RequestParam("userId") userId: Long?,
        @RequestParam("from", required = true) from: LocalDateTime,
        @RequestParam("to", required = true) to: LocalDateTime
    ): ResponseEntity<BrnResponse<List<StudyHistoryDto>>> {
        val result = if (userId != null && roleService.isCurrentUserAdmin()) {
            studyHistoryService.getHistories(userId, from, to)
        } else {
            studyHistoryService.getHistoriesForCurrentUser(from, to)
        }
        return ResponseEntity.ok().body(BrnResponse(data = result))
    }

    @GetMapping("/user/{userId}/has/statistics")
    @Operation(summary = "Check if user has statistics")
    fun isUserHasStatistics(
        @PathVariable("userId") userId: Long
    ) = ResponseEntity.ok()
        .body(BrnResponse(data = studyHistoryService.isUserHasStatistics(userId)))
}
