package com.epam.brn.integration

import com.epam.brn.job.UserDailyAnalyticsJob
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
    fun `should fill a daily analytics snapshot for a user`() {
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
        val today = LocalDate.now().atStartOfDay()
        val firstStudyHistory = insertDefaultStudyHistory(user, exerciseFirst, today.plusHours(9L).truncatedTo(ChronoUnit.SECONDS))
        val secondStudyHistory = insertDefaultStudyHistory(user, exerciseSecond, today.plusHours(11L).truncatedTo(ChronoUnit.SECONDS))

        // WHEN
        userDailyAnalyticsJob.fillUserDailyAnalytics()

        // THEN
        val userDailyAnalyticsList = userDailyAnalyticsRepository.findAll()
        userDailyAnalyticsList.forExactly(1) {
            it.day shouldBe LocalDate.now()
            it.userId shouldBe user.id
            it.firstDone shouldBe firstStudyHistory.startTime
            it.lastDone shouldBe secondStudyHistory.startTime
            it.spentTime shouldBe (firstStudyHistory.spentTimeInSeconds ?: 0L) + (secondStudyHistory.spentTimeInSeconds ?: 0L)
            it.doneExercises shouldBe 2
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
                .atStartOfDay()
                .plusHours(10L)
                .truncatedTo(ChronoUnit.SECONDS),
        )

        // WHEN the job runs twice on the same day
        userDailyAnalyticsJob.fillUserDailyAnalytics()
        userDailyAnalyticsJob.fillUserDailyAnalytics()

        // THEN today's snapshot is replaced, not duplicated
        userDailyAnalyticsRepository.findByDay(LocalDate.now()).size shouldBe 1
    }
}
