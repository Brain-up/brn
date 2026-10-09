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
    fun `should replace today's snapshot and insert fresh rows`() {
        // GIVEN
        every { jdbcTemplate.update(any<String>()) } returns 1

        // WHEN
        userAnalyticsJob.fillUserAnalytics()

        // THEN: one delete of today's rows + one insert of the new snapshot
        verify(exactly = 2) { jdbcTemplate.update(any<String>()) }
    }

    @Test
    fun `should swallow exceptions so the scheduler keeps running`() {
        // GIVEN
        every { jdbcTemplate.update(any<String>()) } throws RuntimeException("db down")

        // WHEN / THEN: no exception propagates out of the job
        userAnalyticsJob.fillUserAnalytics()
    }
}
