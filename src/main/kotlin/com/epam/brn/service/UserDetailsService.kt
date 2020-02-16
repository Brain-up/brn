package com.epam.brn.service

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.UserDetails
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserDetailsRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.server.Session
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class UserDetailsService(@Autowired val userDetailsDAO: UserDetailsRepository) {

    @Autowired val studyHistoryRepository: StudyHistoryRepository,
    private val log = logger()

    fun getLevel(userId: String): Int {
        log.info("current progress = 1")
        return 1
    }

    // TODO implement method add user

    fun findUserDetails(name: String): UserDetails? {
        return userDetailsDAO.findByNameLike(name).first()
    }

    fun getWorkTime() : Long {
        log.info("Calculating time spend on tasks ...")

        val studyHistoryDto: StudyHistoryDto



        //val principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        //val session = Session.Cookie.

        val existingStudyHistory = studyHistoryRepository.findByUserAccountIdAndExerciseId(
            studyHistoryDto.userId,
            studyHistoryDto.exerciseId
        )
        val studyHistory = existingStudyHistory.map { studyHistoryEntity ->
            log.debug("Replacing $studyHistoryDto")
            studyHistoryConverter.updateStudyHistory(studyHistoryDto, studyHistoryEntity)
            studyHistoryDto.responseCode = HttpStatus.OK
            studyHistoryEntity
        }
    }
}
