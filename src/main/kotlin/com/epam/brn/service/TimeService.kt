package com.epam.brn.service

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class TimeService {
    fun now(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
}
