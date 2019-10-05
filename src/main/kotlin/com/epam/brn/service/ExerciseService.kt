package com.epam.brn.service

import com.epam.brn.model.UserDetails
import com.epam.brn.repo.UserDetailsRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ExerciseService(@Autowired val userDetailsDAO: UserDetailsRepository) {

    private val log = logger()

    fun findUserDetails(name: String): UserDetails? {
        return userDetailsDAO.findByNameLike(name).first()
    }
}