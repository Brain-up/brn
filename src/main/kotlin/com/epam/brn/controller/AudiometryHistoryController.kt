package com.epam.brn.controller

import com.epam.brn.dto.request.AudiometryHistoryRequest
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.service.AudiometryHistoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping("/audiometry-history")
@Tag(name = "Audiometry History", description = "Contains actions for audiometry history")
@RolesAllowed(BrnRole.USER)
class AudiometryHistoryController(private val audiometryHistoryService: AudiometryHistoryService) {

    @PostMapping
    @Operation(summary = "Save speech audiometry history")
    fun save(@Validated @RequestBody audiometryHistory: AudiometryHistoryRequest): ResponseEntity<BrnResponse<Long>> =
        ResponseEntity.ok().body(BrnResponse(data = audiometryHistoryService.save(audiometryHistory)))
}
