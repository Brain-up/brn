package com.epam.brn.controller

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.constant.BrnPath
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BrnPath.STUDY_HISTORY)
@Api(value = BrnPath.STUDY_HISTORY, description = "Contains the results of a finished exercise")
class StudyHistoryController(@Autowired val studyHistoryService: StudyHistoryService) {

    @PostMapping("/createStudyHistory")
    fun createStudyHistory(@Validated @RequestBody studyHistoryDto: StudyHistoryDto) {
        studyHistoryService.saveStudyHistory(studyHistoryDto)
    }
}