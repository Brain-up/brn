package com.brn.service

import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class UserDetailService {

    private val log = logger()

    fun getLevel(userId: String): Int {
        log.info("current level = 1")
        return 1
    }

    fun updateLevel(userId: String, newLevel: Int) {
    }
}