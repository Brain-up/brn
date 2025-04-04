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
        userAccountRepository.deleteAll()
    }

    @Test
    fun `test filling user analytics job`() {
        // GIVEN
        val roleName = "USER"
        val role = createRole(roleName)
        val user = insertDefaultUser()
        user.roleSet.add(role)
        userAccountRepository.save(user)

        val firstName = "FirstName"
        val secondName = "SecondName"
        val existingSeries = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(existingSeries, 1)
        val exerciseFirst = insertDefaultExercise(subGroup, firstName)
        val exerciseSecond = insertDefaultExercise(subGroup, secondName)
        val now = LocalDateTime.now()
        val firstStudyHistory = insertDefaultStudyHistory(user, exerciseFirst, now.minusHours(1L).truncatedTo(ChronoUnit.SECONDS))
        val secondStudyHistory = insertDefaultStudyHistory(user, exerciseSecond, now.plusHours(1L).truncatedTo(ChronoUnit.SECONDS))

        // WHEN
        userAnalyticsJob.fillUserAnalytics()

        // THEN
        val userAnalyticsList = userAnalyticsRepository.findAll()
        userAnalyticsList.forExactly(1) {
            it.userId shouldBe user.id
            it.firstDone shouldBe firstStudyHistory.startTime
            it.lastDone shouldBe secondStudyHistory.startTime
            it.spentTime shouldBe (firstStudyHistory.spentTimeInSeconds ?: 0L) + (secondStudyHistory.spentTimeInSeconds ?: 0L)
            it.doneExercises shouldBe 2
            it.studyDays shouldBe 1
            it.roleName shouldBe roleName
        }
    }
}
