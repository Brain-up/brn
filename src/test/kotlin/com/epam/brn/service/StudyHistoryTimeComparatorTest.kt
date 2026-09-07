package com.epam.brn.service

import com.epam.brn.model.StudyHistory
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
internal class StudyHistoryTimeComparatorTest {
    @InjectMockKs
    private lateinit var comparator: StudyHistoryTimeComparator

    @MockK
    private lateinit var studyHistory1: StudyHistory

    @MockK
    private lateinit var studyHistory2: StudyHistory

    @Test
    fun `compare should return -1 when first studyHistory start date is before second studyHistory date`() {
        val startDate = LocalDateTime.now()
        every { studyHistory1.startTime } returns startDate
        every { studyHistory2.startTime } returns startDate.plusDays(1)

        val comparingResult = comparator.compare(studyHistory1, studyHistory2)

        comparingResult shouldBe -1
    }

    @Test
    fun `compare should return 1 when first studyHistory start date is after second studyHistory date`() {
        val startDate = LocalDateTime.now()
        every { studyHistory1.startTime } returns startDate.plusDays(1)
        every { studyHistory2.startTime } returns startDate

        val comparingResult = comparator.compare(studyHistory1, studyHistory2)

        comparingResult shouldBe 1
    }

    @Test
    fun `compare should return 0 when first studyHistory start date equals to second studyHistory date`() {
        val startDate = LocalDateTime.now()
        every { studyHistory1.startTime } returns startDate
        every { studyHistory2.startTime } returns startDate

        val comparingResult = comparator.compare(studyHistory1, studyHistory2)

        comparingResult shouldBe 0
    }
}
