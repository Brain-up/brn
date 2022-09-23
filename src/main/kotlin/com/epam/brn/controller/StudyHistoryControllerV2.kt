package com.epam.brn.controller

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.response.Response
import com.epam.brn.enums.AuthorityType
import com.epam.brn.enums.RoleConstants
import com.epam.brn.service.StudyHistoryService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
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
@Api(value = "/v2/study-history", tags = ["Study History"], description = "Contains actions over the results of finished exercise")
@RolesAllowed(RoleConstants.USER)
class StudyHistoryControllerV2(
    @Autowired val studyHistoryService: StudyHistoryService,
    @Autowired val authorityService: AuthorityService
) {
    @GetMapping("/histories")
    @ApiOperation("Get user's study histories for period from <= startTime <= to where startTime is a date in ISO date time format")
    fun getHistories(
        @RequestParam("userId") userId: Long?,
        @RequestParam("from", required = true) from: LocalDateTime,
        @RequestParam("to", required = true) to: LocalDateTime
    ): ResponseEntity<Response<List<StudyHistoryDto>>> {
        val result = if (userId != null && authorityService.isCurrentUserHasAuthority(AuthorityType.ROLE_ADMIN)) {
            studyHistoryService.getHistories(userId, from, to)
        } else {
            studyHistoryService.getHistoriesForCurrentUser(from, to)
        }
        return ResponseEntity.ok().body(Response(data = result))
    }

    @GetMapping("/user/{userId}/has/statistics")
    @ApiOperation("Check if user has statistics")
    fun isUserHasStatistics(
        @PathVariable("userId") userId: Long
    ) = ResponseEntity.ok()
        .body(Response(data = studyHistoryService.isUserHasStatistics(userId)))
}
