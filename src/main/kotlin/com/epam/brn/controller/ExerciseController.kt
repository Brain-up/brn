package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.service.ExerciseService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import javax.servlet.http.HttpServletRequest
import org.keycloak.KeycloakSecurityContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exercises")
@Api(value = "/exercises", description = "Contains actions over exercises")
class ExerciseController(@Autowired val exerciseService: ExerciseService) {

    @GetMapping
    @ApiOperation("Get done exercises for user")
    fun getExercises(
        request: HttpServletRequest,
        @RequestParam(value = "seriesId", required = true) seriesId: Long
    ): ResponseEntity<BaseResponseDto> {
        val keycloakSecurityContext: KeycloakSecurityContext = request.getAttribute(KeycloakSecurityContext::class.java.name) as KeycloakSecurityContext
        val userId = keycloakSecurityContext.token.subject
        return ResponseEntity.ok()
            .body(BaseResponseDto(data = exerciseService.findExercisesByUserIdAndSeries(userId, seriesId)))
    }

    @GetMapping(value = ["/{exerciseId}"])
    @ApiOperation("Get exercise by id")
    fun getExercisesByID(
        @PathVariable("exerciseId") exerciseId: Long
    ): ResponseEntity<BaseSingleObjectResponseDto> {
        return ResponseEntity.ok()
            .body(BaseSingleObjectResponseDto(data = exerciseService.findExerciseById(exerciseId)))
    }
}
