package com.epam.brn.service

import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class TimeService {
    fun now(): ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
}
