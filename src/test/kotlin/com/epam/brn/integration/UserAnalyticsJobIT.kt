package com.epam.brn.integration

import com.epam.brn.job.UserAnalyticsJob
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.repo.UserAnalyticsRepository
import io.kotest.inspectors.forExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class UserAnalyticsJobIT : BaseIT() {
    @Autowired
    lateinit var userAnalyticsJob: UserAnalyticsJob

    @Autowired
    lateinit var userAnalyticsRepository: UserAnalyticsRepository

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @AfterEach
    fun deleteAfterTest() {
        userAnalyticsRepository.deleteAll()
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
        val now = LocalDateTime.now()
        val firstStudyHistory = insertDefaultStudyHistory(user, exerciseFirst, now.minusHours(1L).truncatedTo(ChronoUnit.SECONDS))
        val secondStudyHistory = insertDefaultStudyHistory(user, exerciseSecond, now.plusHours(1L).truncatedTo(ChronoUnit.SECONDS))

        // WHEN
        userAnalyticsJob.fillUserAnalytics()

        // THEN
        val userAnalyticsList = userAnalyticsRepository.findAll()
        userAnalyticsList.forExactly(1) {
            it.snapshotDate shouldBe LocalDate.now()
            it.userId shouldBe user.id
            it.firstDone shouldBe firstStudyHistory.startTime
            it.lastDone shouldBe secondStudyHistory.startTime
            it.spentTime shouldBe (firstStudyHistory.spentTimeInSeconds ?: 0L) + (secondStudyHistory.spentTimeInSeconds ?: 0L)
            it.doneExercises shouldBe 2
            it.studyDays shouldBe 1
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
        insertDefaultStudyHistory(user, exercise, LocalDateTime.now().minusHours(1L).truncatedTo(ChronoUnit.SECONDS))

        // WHEN the job runs twice on the same day
        userAnalyticsJob.fillUserAnalytics()
        userAnalyticsJob.fillUserAnalytics()

        // THEN today's snapshot is replaced, not duplicated
        userAnalyticsRepository.findBySnapshotDate(LocalDate.now()).size shouldBe 1
    }
}
