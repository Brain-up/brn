package com.epam.brn.job

import com.epam.brn.service.UserAccountService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class AutoTestUsersDeletionJob(val userAccountService: UserAccountService) {
    private val log = logger()

    @Scheduled(cron = "@midnight")
    fun deleteAutoTestUsers() {
        val usersCount = userAccountService.deleteAutoTestUsers()
        log.info("Deleted $usersCount autotest users")
    }
}
