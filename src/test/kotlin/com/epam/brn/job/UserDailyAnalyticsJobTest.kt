package com.epam.brn.job

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.jdbc.core.JdbcTemplate

@ExtendWith(MockKExtension::class)
class UserDailyAnalyticsJobTest {
    @InjectMockKs
    lateinit var userDailyAnalyticsJob: UserDailyAnalyticsJob

    @MockK(relaxed = true, relaxUnitFun = true)
    lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `should replace today's rows and insert fresh deltas`() {
        // GIVEN
        every { jdbcTemplate.update(any<String>()) } returns 1

        // WHEN
        userDailyAnalyticsJob.fillUserDailyAnalytics()

        // THEN: one delete of today's rows + one insert of the fresh delta
        verify(exactly = 2) { jdbcTemplate.update(any<String>()) }
    }

    @Test
    fun `should propagate exceptions so the transaction rolls back and the failure is logged`() {
        // GIVEN
        every { jdbcTemplate.update(any<String>()) } throws RuntimeException("db down")

        // WHEN / THEN: the failure is not swallowed
        shouldThrow<RuntimeException> {
            userDailyAnalyticsJob.fillUserDailyAnalytics()
        }
    }
}
