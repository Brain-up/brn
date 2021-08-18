package com.epam.brn.dto

import com.epam.brn.model.Exercise
import com.epam.brn.model.UserAccount
import io.kotest.matchers.comparables.shouldBeLessThan
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.math.abs

@ExtendWith(MockKExtension::class)
internal class StudyHistoryDtoTest {

    @Test
    fun `should test toEntity`() {
        // GIVEN
        val dto = StudyHistoryDto(
            id = 1L,
            exerciseId = 1L,
            startTime = LocalDateTime.now().minusMinutes(1),
            endTime = LocalDateTime.now(),
            executionSeconds = 60,
            tasksCount = 4,
            replaysCount = 2,
            wrongAnswers = 1
        )
        val userAccount = mockk<UserAccount>()
        val exercise = mockk<Exercise>()
        // WHEN
        val studyHistory = dto.toEntity(userAccount, exercise)
        // THEN
        studyHistory.rightAnswersIndex shouldBe 3.0 / 4
        abs(studyHistory.repetitionIndex!! - 2.0 / 6) shouldBeLessThan 0.00000001
    }
}
