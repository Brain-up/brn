package com.epam.brn.service.impl

import com.epam.brn.enums.BrnGender
import com.epam.brn.model.projection.UsersWithAnalyticsView
import com.epam.brn.repo.UserAnalyticsRepository
import io.kotest.inspectors.forExactly
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val ONE = 1
private const val ONE_LONG = 1L

@ExtendWith(MockKExtension::class)
class UserAnalyticsServiceV1ImplTest {

    @InjectMockKs
    lateinit var service: UserAnalyticsServiceV1Impl

    @MockK
    lateinit var userAnalyticsRepository: UserAnalyticsRepository

    @MockK
    lateinit var pageable: Pageable

    @MockK
    lateinit var usersWithAnalyticsView: UsersWithAnalyticsView

    @Test
    fun getUsersWithAnalytics() {
        // GIVEN
        val role = "USER"
        val now = LocalDateTime.now()
        val firstDone = now.minusHours(ONE_LONG).truncatedTo(ChronoUnit.SECONDS)
        val lastDone = now.plusHours(ONE_LONG).truncatedTo(ChronoUnit.SECONDS)
        val bornYear = 2025

        every { usersWithAnalyticsView.id } returns ONE_LONG
        every { usersWithAnalyticsView.userId } returns "1"
        every { usersWithAnalyticsView.fullName } returns "Test User"
        every { usersWithAnalyticsView.email } returns "test@test.com"
        every { usersWithAnalyticsView.bornYear } returns bornYear
        every { usersWithAnalyticsView.gender } returns BrnGender.MALE
        every { usersWithAnalyticsView.active } returns true
        every { usersWithAnalyticsView.firstDone } returns firstDone
        every { usersWithAnalyticsView.lastDone } returns lastDone
        every { usersWithAnalyticsView.lastVisit } returns now
        every { usersWithAnalyticsView.doneExercises } returns ONE
        every { usersWithAnalyticsView.spentTime } returns ONE_LONG
        every { usersWithAnalyticsView.studyDays } returns ONE

        every { userAnalyticsRepository.getUserAnalytics(any(), any()) } returns listOf(usersWithAnalyticsView)

        // WHEN
        val usersWithAnalytics = service.getUsersWithAnalytics(pageable, role)

        // THEN
        verify(exactly = ONE) { userAnalyticsRepository.getUserAnalytics(pageable, role) }

        usersWithAnalytics.forExactly(ONE) {
            it.id shouldBe usersWithAnalyticsView.id
            it.userId shouldBe usersWithAnalyticsView.userId
            it.name shouldBe usersWithAnalyticsView.fullName
            it.email shouldBe usersWithAnalyticsView.email
            it.bornYear shouldBe usersWithAnalyticsView.bornYear
            it.gender shouldBe usersWithAnalyticsView.gender
            it.active shouldBe usersWithAnalyticsView.active
            it.firstDone shouldBe usersWithAnalyticsView.firstDone
            it.lastDone shouldBe usersWithAnalyticsView.lastDone
            it.lastVisit shouldBe usersWithAnalyticsView.lastVisit
            it.doneExercises shouldBe usersWithAnalyticsView.doneExercises
            it.spentTime shouldBeEqualTo usersWithAnalyticsView.spentTime.toDuration(DurationUnit.SECONDS)
            it.studyDaysInCurrentMonth shouldBe usersWithAnalyticsView.studyDays
        }
    }
}
