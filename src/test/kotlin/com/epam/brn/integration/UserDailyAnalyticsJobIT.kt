package com.epam.brn.integration

import com.epam.brn.job.UserDailyAnalyticsJob
import com.epam.brn.model.UserDailyAnalytics
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.repo.UserDailyAnalyticsRepository
import io.kotest.inspectors.forExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class UserDailyAnalyticsJobIT : BaseIT() {
    @Autowired
    lateinit var userDailyAnalyticsJob: UserDailyAnalyticsJob

    @Autowired
    lateinit var userDailyAnalyticsRepository: UserDailyAnalyticsRepository

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @AfterEach
    fun deleteAfterTest() {
        userDailyAnalyticsRepository.deleteAll()
        deleteInsertedTestData()
    }

    @Test
    fun `should fill a daily analytics delta for a user`() {
        // GIVEN
        val roleName = "USER"
        val role = createRole(roleName)
        val user = insertDefaultUser()
        user.roleSet.add(role)
        userAccountRepository.save(user)

        val existingSeries = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(existingSeries, 1)
        val exerciseFirst = insertDefaultExercise(subGroup, "FirstName")
        val exerciseSecond = insertDefaultExercise(subGroup, "SecondName")
        val yesterday = LocalDate.now().minusDays(1).atStartOfDay()
        val firstStudyHistory = insertDefaultStudyHistory(user, exerciseFirst, yesterday.plusHours(9L).truncatedTo(ChronoUnit.SECONDS))
        val secondStudyHistory = insertDefaultStudyHistory(user, exerciseSecond, yesterday.plusHours(11L).truncatedTo(ChronoUnit.SECONDS))
        // a repeat of the first exercise same day: counts toward done_exercises but not unique_done_exercises
        val repeatStudyHistory = insertDefaultStudyHistory(user, exerciseFirst, yesterday.plusHours(13L).truncatedTo(ChronoUnit.SECONDS))

        // WHEN
        userDailyAnalyticsJob.fillUserDailyAnalytics()

        // THEN
        val userDailyAnalyticsList = userDailyAnalyticsRepository.findAll()
        userDailyAnalyticsList.forExactly(1) {
            it.day shouldBe LocalDate.now().minusDays(1)
            it.userId shouldBe user.id
            it.firstDone shouldBe firstStudyHistory.startTime
            it.lastDone shouldBe repeatStudyHistory.startTime
            it.spentTime shouldBe
                (firstStudyHistory.spentTimeInSeconds ?: 0L) +
                (secondStudyHistory.spentTimeInSeconds ?: 0L) +
                (repeatStudyHistory.spentTimeInSeconds ?: 0L)
            it.executionTime shouldBe
                (firstStudyHistory.executionSeconds + secondStudyHistory.executionSeconds + repeatStudyHistory.executionSeconds).toLong()
            it.doneExercises shouldBe 3
            it.uniqueDoneExercises shouldBe 2
            it.roleName shouldBe roleName
        }
    }

    @Test
    fun `should be idempotent when run twice on the same day`() {
        // GIVEN
        val role = createRole("USER")
        val user = insertDefaultUser()
        user.roleSet.add(role)
        userAccountRepository.save(user)

        val subGroup = insertDefaultSubGroup(insertDefaultSeries(), 1)
        val exercise = insertDefaultExercise(subGroup, "FirstName")
        insertDefaultStudyHistory(
            user,
            exercise,
            LocalDate
                .now()
                .minusDays(1)
                .atStartOfDay()
                .plusHours(10L)
                .truncatedTo(ChronoUnit.SECONDS),
        )

        // WHEN the job runs twice for the same day
        userDailyAnalyticsJob.fillUserDailyAnalytics()
        userDailyAnalyticsJob.fillUserDailyAnalytics()

        // THEN that day's rows are replaced, not duplicated
        userDailyAnalyticsRepository.findByDay(LocalDate.now().minusDays(1)).size shouldBe 1
    }

    @Test
    fun `should heal drift on a recent day within the window and leave days outside it untouched`() {
        // GIVEN a user with real history three days ago (inside the default 7-day window)
        val roleName = "USER"
        val role = createRole(roleName)
        val user = insertDefaultUser()
        user.roleSet.add(role)
        userAccountRepository.save(user)

        val subGroup = insertDefaultSubGroup(insertDefaultSeries(), 1)
        val exerciseFirst = insertDefaultExercise(subGroup, "FirstName")
        val exerciseSecond = insertDefaultExercise(subGroup, "SecondName")

        val threeDaysAgo = LocalDate.now().minusDays(3)
        val h1 = insertDefaultStudyHistory(user, exerciseFirst, threeDaysAgo.atTime(9, 0).truncatedTo(ChronoUnit.SECONDS))
        val h2 = insertDefaultStudyHistory(user, exerciseSecond, threeDaysAgo.atTime(11, 0).truncatedTo(ChronoUnit.SECONDS))

        // AND a deliberately drifted stored row for that day, plus a stale row outside the window (no history)
        val tenDaysAgo = LocalDate.now().minusDays(10)
        userDailyAnalyticsRepository.save(
            UserDailyAnalytics(
                day = threeDaysAgo,
                userId = user.id!!,
                roleName = roleName,
                firstDone = threeDaysAgo.atTime(1, 0),
                lastDone = threeDaysAgo.atTime(2, 0),
                spentTime = 1L,
                executionTime = 1L,
                doneExercises = 999,
                uniqueDoneExercises = 999,
            ),
        )
        userDailyAnalyticsRepository.save(
            UserDailyAnalytics(
                day = tenDaysAgo,
                userId = user.id!!,
                roleName = roleName,
                firstDone = tenDaysAgo.atTime(1, 0),
                lastDone = tenDaysAgo.atTime(2, 0),
                spentTime = 42L,
                executionTime = 42L,
                doneExercises = 42,
                uniqueDoneExercises = 42,
            ),
        )

        // WHEN the reconciler runs
        userDailyAnalyticsJob.fillUserDailyAnalytics()

        // THEN the drifted in-window row is recomputed from study_history
        val healed = userDailyAnalyticsRepository.findByDay(threeDaysAgo)
        healed.size shouldBe 1
        healed.first().doneExercises shouldBe 2
        healed.first().uniqueDoneExercises shouldBe 2
        healed.first().spentTime shouldBe (h1.spentTimeInSeconds ?: 0L) + (h2.spentTimeInSeconds ?: 0L)
        healed.first().executionTime shouldBe (h1.executionSeconds + h2.executionSeconds).toLong()
        healed.first().firstDone shouldBe h1.startTime
        healed.first().lastDone shouldBe h2.startTime

        // AND the stale out-of-window row is left untouched
        val untouched = userDailyAnalyticsRepository.findByDay(tenDaysAgo)
        untouched.size shouldBe 1
        untouched.first().doneExercises shouldBe 42
    }
}
