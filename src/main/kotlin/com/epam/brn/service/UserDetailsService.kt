package com.epam.brn.service

import com.epam.brn.model.UserDetails
import com.epam.brn.repo.UserDetailsRepository
import org.apache.logging.log4j.kotlin.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserDetailsService(@Autowired val userDetailsDAO: UserDetailsRepository) {

    private val log = logger()

    fun getLevel(userId: String): Int {
        log.info("current progress = 1")
        return 1
    }

    // TODO implement method add user

    fun findUserDetails(name: String): UserDetails? {
        return userDetailsDAO.findByNameLike(name).first()
    }

    fun updateLevel(userId: String, exerciseId: Int) {
    }
}