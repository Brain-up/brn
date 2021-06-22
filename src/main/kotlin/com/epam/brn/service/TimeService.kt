package com.epam.brn.service

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TimeService {
    fun now(): LocalDateTime = LocalDateTime.now()
}
