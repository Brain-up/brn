package com.epam.brn.integration

import com.epam.brn.job.UserDailyAnalyticsJob
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.repo.UserDailyAnalyticsRepository
import com.epam.brn.service.statistics.impl.UserDayStatisticsService
import com.epam.brn.service.statistics.impl.UserMonthStatisticsService
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Validation gate for serving the period statistics graphs from the precompute store (phase 2 of
 * #2609). Proves that `user_daily_analytics` carries exactly what `UserDayStatisticsService` and
 * `UserMonthStatisticsService` derive from raw `study_history`, so a later change can switch those
 * read paths onto the store without altering the output:
 *   - day point  = SUM(execution_seconds) per day, dated at the day's earliest start  -> execution_time / first_done
 *   - month point = SUM(execution_seconds), COUNT(distinct day), earliest start        -> SUM(execution_time) / COUNT(*) / MIN(first_done)
 * `progress` is a pure function of those drivers (a threshold over the per-day execution sums and the
 * active-day set), so equal drivers imply equal progress; we assert the drivers the store holds.
 *
 * The reconcile window is widened so the nightly reconciler populates the whole seeded range (it
 * normally only heals the trailing week and never today — all seeded days are strictly before today).
 */
@TestPropertySource(properties = ["brn.user.analytics.precompute.reconcile-days=400"])
class UserDailyAnalyticsPeriodEquivalenceIT : BaseIT() {
    @Autowired
    lateinit var userDailyAnalyticsJob: UserDailyAnalyticsJob

    @Autowired
    lateinit var userDailyAnalyticsRepository: UserDailyAnalyticsRepository

    @Autowired
    lateinit var userDayStatisticsService: UserDayStatisticsService

    @Autowired
    lateinit var userMonthStatisticsService: UserMonthStatisticsService

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @AfterEach
    fun deleteAfterTest() {
        userDailyAnalyticsRepository.deleteAll()
        deleteInsertedTestData()
    }

    @Test
    fun `store reproduces the period statistics services across several days and months`() {
        // GIVEN a user with activity on several past days spanning multiple calendar months,
        // including a same-day repeat (counts toward time but not the day count)
        val user = insertDefaultUser()
        user.roleSet.add(createRole("USER"))
        userAccountRepository.save(user)

        val subGroup = insertDefaultSubGroup(insertDefaultSeries(), 1)
        val exerciseA = insertDefaultExercise(subGroup, "A")
        val exerciseB = insertDefaultExercise(subGroup, "B")

        fun at(
            daysAgo: Long,
            hour: Int,
        ) = LocalDate
            .now()
            .minusDays(daysAgo)
            .atTime(hour, 0)
            .truncatedTo(ChronoUnit.SECONDS)

        insertDefaultStudyHistory(user, exerciseA, at(3, 9))
        insertDefaultStudyHistory(user, exerciseB, at(3, 11)) // same day, second exercise
        insertDefaultStudyHistory(user, exerciseA, at(35, 10))
        insertDefaultStudyHistory(user, exerciseA, at(35, 14)) // same day, repeat of A
        insertDefaultStudyHistory(user, exerciseB, at(70, 12))

        userDailyAnalyticsJob.fillUserDailyAnalytics()

        val from = LocalDate.now().minusDays(100).atStartOfDay()
        val to = LocalDate.now().minusDays(1).atTime(23, 59, 59)

        val storeRows = userDailyAnalyticsRepository.findAll().filter { it.userId == user.id }

        // WHEN the period services aggregate raw study_history
        val liveDays = userDayStatisticsService.getStatisticsForPeriod(from, to, user.id)
        val liveMonths = userMonthStatisticsService.getStatisticsForPeriod(from, to, user.id)

        // THEN the store reproduces both the per-day and per-month drivers exactly
        assertSoftly {
            // day: execution seconds per day
            liveDays.associate { it.date.toLocalDate() to it.exercisingTimeSeconds.toLong() } shouldBe
                storeRows.associate { it.day to it.executionTime }
            // day: the point is dated at the day's earliest start
            liveDays.associate { it.date.toLocalDate() to it.date } shouldBe
                storeRows.associate { it.day to it.firstDone }

            // month: execution seconds, active-day count, and earliest start per calendar month
            liveMonths.associate {
                YearMonth.from(it.date) to Triple(it.exercisingTimeSeconds.toLong(), it.exercisingDays, it.date)
            } shouldBe
                storeRows.groupBy { YearMonth.from(it.day) }.mapValues { (_, rows) ->
                    Triple(rows.sumOf { it.executionTime ?: 0L }, rows.size, rows.minOf { it.firstDone as LocalDateTime })
                }
        }
    }
}
