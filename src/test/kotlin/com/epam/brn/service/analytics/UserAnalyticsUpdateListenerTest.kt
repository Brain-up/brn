package com.epam.brn.service.analytics

import com.epam.brn.event.ExerciseCompletedEvent
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
internal class UserAnalyticsUpdateListenerTest {
    @InjectMockKs
    lateinit var listener: UserAnalyticsUpdateListener

    @MockK
    lateinit var userAnalyticsUpdater: UserAnalyticsUpdater

    @Test
    fun `should delegate the completed-exercise event to the async updater`() {
        // GIVEN
        val event =
            ExerciseCompletedEvent(
                userId = 42L,
                exerciseId = 5L,
                activityTime = LocalDateTime.now(),
                spentSeconds = 10L,
                executionSeconds = 8L,
            )
        every { userAnalyticsUpdater.apply(event) } just runs

        // WHEN
        listener.onExerciseCompleted(event)

        // THEN
        verify(exactly = 1) { userAnalyticsUpdater.apply(event) }
    }
}
