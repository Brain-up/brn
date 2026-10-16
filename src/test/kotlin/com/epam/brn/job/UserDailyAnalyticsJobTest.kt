package com.epam.brn.job

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.jdbc.core.JdbcTemplate

@ExtendWith(MockKExtension::class)
class UserDailyAnalyticsJobTest {
    @MockK(relaxed = true, relaxUnitFun = true)
    lateinit var jdbcTemplate: JdbcTemplate

    private val reconcileDays = 7

    private val userDailyAnalyticsJob by lazy { UserDailyAnalyticsJob(jdbcTemplate, reconcileDays) }

    @Test
    fun `should delete and recompute the reconcile window from study_history`() {
        // GIVEN
        every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

        // WHEN
        userDailyAnalyticsJob.fillUserDailyAnalytics()

        // THEN: one delete of the window + one insert of the recomputed deltas, both parameterised by the window size
        verify(exactly = 2) { jdbcTemplate.update(any<String>(), reconcileDays) }
    }

    @Test
    fun `should propagate exceptions so the transaction rolls back and the failure is logged`() {
        // GIVEN
        every { jdbcTemplate.update(any<String>(), *anyVararg()) } throws RuntimeException("db down")

        // WHEN / THEN: the failure is not swallowed
        shouldThrow<RuntimeException> {
            userDailyAnalyticsJob.fillUserDailyAnalytics()
        }
    }
}
