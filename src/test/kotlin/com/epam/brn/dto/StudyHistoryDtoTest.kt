package com.epam.brn.dto

import com.epam.brn.model.Exercise
import com.epam.brn.model.UserAccount
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.math.abs

@ExtendWith(MockKExtension::class)
internal class StudyHistoryDtoTest {
    @Test
    fun `should test toEntity`() {
        // GIVEN
        val dto =
            StudyHistoryDto(
                id = 1L,
                exerciseId = 1L,
                startTime = LocalDateTime.now().minusMinutes(1),
                endTime = LocalDateTime.now(),
                executionSeconds = 60,
                tasksCount = 4,
                replaysCount = 2,
                wrongAnswers = 1,
            )
        val userAccount = mockk<UserAccount>()
        val exercise = mockk<Exercise>()
        // WHEN
        val studyHistory = dto.toEntity(userAccount, exercise)
        // THEN
        studyHistory.rightAnswersIndex shouldBe 3.0 / 4
        abs(studyHistory.repetitionIndex!! - 2.0 / 6) shouldBeLessThan 0.00000001
    }

    @Test
    fun `stored rightAnswersIndex equals the one-minus-wrong-share form used by the done-well judgement`() {
        // GIVEN — the save formula (tasksCount - wrongAnswers) / tasksCount and the judgement
        // formula 1 - wrongAnswers / tasksCount are algebraically equal; pin that equivalence.
        val tasksCount: Short = 4
        val wrongAnswers = 1
        val dto =
            StudyHistoryDto(
                id = 1L,
                exerciseId = 1L,
                startTime = LocalDateTime.now().minusMinutes(1),
                endTime = LocalDateTime.now(),
                executionSeconds = 60,
                tasksCount = tasksCount,
                replaysCount = 0,
                wrongAnswers = wrongAnswers,
            )

        // WHEN
        val studyHistory = dto.toEntity(mockk<UserAccount>(), mockk<Exercise>())

        // THEN
        studyHistory.rightAnswersIndex shouldBe 1F - wrongAnswers.toFloat() / tasksCount
    }

    @Test
    fun `stored repetitionIndex and the judged repetition index are complements summing to one`() {
        // GIVEN — stored index = replays/(tasks+replays); the done-well judgement instead uses
        // tasks/(replays+tasks). For the same attempt they are complements; pin the divergence.
        val tasksCount: Short = 3
        val replaysCount = 1
        val dto =
            StudyHistoryDto(
                id = 1L,
                exerciseId = 1L,
                startTime = LocalDateTime.now().minusMinutes(1),
                endTime = LocalDateTime.now(),
                executionSeconds = 60,
                tasksCount = tasksCount,
                replaysCount = replaysCount,
                wrongAnswers = 0,
            )

        // WHEN
        val storedRepetitionIndex = dto.toEntity(mockk<UserAccount>(), mockk<Exercise>()).repetitionIndex!!
        val judgedRepetitionIndex = tasksCount.toFloat() / (replaysCount + tasksCount)

        // THEN
        storedRepetitionIndex + judgedRepetitionIndex shouldBe 1.0f
    }
}
