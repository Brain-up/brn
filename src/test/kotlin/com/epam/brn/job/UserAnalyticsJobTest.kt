package com.epam.brn.job

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.jdbc.core.JdbcTemplate

@ExtendWith(MockKExtension::class)
class UserAnalyticsJobTest {
    @InjectMockKs
    lateinit var userAnalyticsJob: UserAnalyticsJob

    @MockK(relaxed = true, relaxUnitFun = true)
    lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun fillStudyAnalytics() {
        // GIVEN
        every { jdbcTemplate.update(any<String>()) }.returns(1)

        // WHEN
        userAnalyticsJob.fillUserAnalytics()

        // THEN
        verify(exactly = 1) { jdbcTemplate.update(any<String>()) }
    }
}
