package com.epam.brn.upload.csv.nonspeech

import com.epam.brn.enums.Locale
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Series
import com.epam.brn.model.Signal
import com.epam.brn.model.SubGroup
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SubGroupRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SignalSeriesRecordProcessorTest {
    @MockK
    private lateinit var exerciseRepositoryMock: ExerciseRepository

    @MockK
    private lateinit var subGroupRepositoryMock: SubGroupRepository

    @InjectMockKs
    private lateinit var signalSeriesRecordProcessor: SignalSeriesRecordProcessor

    private val exerciseGroup = ExerciseGroup(
        code = "NON_SPEECH_RU_RU",
        name = "Неречевые упражнения",
        description = "Неречевые упражнения"
    )

    private val seriesDuration = Series(
        id = 2L,
        level = 1,
        type = "type",
        name = "Длительность сигналов тест",
        description = "Длительность сигналов тест",
        exerciseGroup = exerciseGroup
    )

    private val subGroupDuration = SubGroup(
        series = seriesDuration,
        level = 1,
        code = "durationSignals",
        name = "subGroup durationSignals"
    )

    private val subGroupFrequency = SubGroup(
        series = seriesDuration,
        level = 1,
        code = "frequencySignals",
        name = "subGroup frequencySignals"
    )

    private val locale = Locale.RU.locale

    @BeforeEach
    internal fun setUp() {
        signalSeriesRecordProcessor = SignalSeriesRecordProcessor(
            subGroupRepositoryMock,
            exerciseRepositoryMock
        )
        every { subGroupRepositoryMock.findByCodeAndLocale("durationSignals", locale) } returns subGroupDuration
    }

    @Test
    fun `should create correct exercise`() {
        val exercise = createExercise(subGroupDuration)
        every { exerciseRepositoryMock.save(exercise) } returns exercise
        every { exerciseRepositoryMock.findByNameAndLevel("По 2 сигнала разной длительности.", 1) } returns null
        val actual = signalSeriesRecordProcessor.process(
            listOf(
                SignalSeriesRecord(
                    exerciseName = "По 2 сигнала разной длительности.",
                    exerciseType = ExerciseType.DURATION_SIGNALS,
                    level = 1,
                    code = "durationSignals",
                    signals = emptyList()
                )
            )
        )

        verify { subGroupRepositoryMock.findByCodeAndLocale("durationSignals", locale) }

        Assertions.assertThat(actual).contains(exercise)
        verify { exerciseRepositoryMock.save(exercise) }
    }

    @Test
    fun `should create correct exercise for different series`() {
        // GIVEN
        val ex1 = createExercise(subGroupDuration)
        val ex2 = createExercise(subGroupFrequency)
        ex2.name = "По 2 сигнала разной частоты."
        every { exerciseRepositoryMock.findByNameAndLevel("По 2 сигнала разной длительности.", 1) } returns null
        every { exerciseRepositoryMock.findByNameAndLevel("По 2 сигнала разной частоты.", 1) } returns null
        every { exerciseRepositoryMock.save(ex1) } returns ex1
        every { exerciseRepositoryMock.save(ex2) } returns ex2
        every { subGroupRepositoryMock.findByCodeAndLocale("subGroupDuration", locale) } returns subGroupDuration
        every { subGroupRepositoryMock.findByCodeAndLocale("subGroupFrequency", locale) } returns subGroupFrequency
        // WHEN
        val actual = signalSeriesRecordProcessor.process(
            listOf(
                SignalSeriesRecord(
                    exerciseName = "По 2 сигнала разной длительности.",
                    exerciseType = ExerciseType.DURATION_SIGNALS,
                    level = 1,
                    code = "subGroupDuration",
                    signals = emptyList()
                ),
                SignalSeriesRecord(
                    exerciseName = "По 2 сигнала разной частоты.",
                    exerciseType = ExerciseType.FREQUENCY_SIGNALS,
                    level = 1,
                    code = "subGroupFrequency",
                    signals = emptyList()
                )
            )
        )
        // THEN
        Assertions.assertThat(actual).containsAll(listOf(ex1, ex2))
        verify { exerciseRepositoryMock.save(ex1) }
        verify { exerciseRepositoryMock.save(ex2) }
    }

    @Test
    fun `should create correct signals for subGroupDuration`() {
        // GIVEN
        val locale = Locale.RU
        val code = "subGroupDuration"
        val record = SignalSeriesRecord(
            exerciseName = "По 2 сигнала разной длительности.",
            exerciseType = ExerciseType.DURATION_SIGNALS,
            level = 1,
            code = code,
            signals = listOf("1000 120", "1000 60")
        )
        every { subGroupRepositoryMock.findByCodeAndLocale(code, locale.locale) } returns subGroupDuration

        val exercise = createExerciseWithSignals(subGroupDuration)
        every { exerciseRepositoryMock.save(exercise) } returns exercise
        every { exerciseRepositoryMock.findByNameAndLevel("По 2 сигнала разной длительности.", 1) } returns null
        // WHEN
        val actual = signalSeriesRecordProcessor.process(listOf(record), locale)
        // THEN
        Assertions.assertThat(actual).contains(exercise)
        verify { exerciseRepositoryMock.save(exercise) }
    }

    private fun createExercise(subGroup: SubGroup) = Exercise(
        subGroup = subGroup,
        name = "По 2 сигнала разной длительности.",
        level = 1
    )

    private fun createExerciseWithSignals(subGroup: SubGroup): Exercise {
        val exercise = Exercise(
            subGroup = subGroup,
            name = "По 2 сигнала разной длительности.",
            level = 1
        )
        val signals = listOf(
            Signal(
                frequency = 1000,
                length = 120,
                exercise = exercise
            ),
            Signal(
                frequency = 1000,
                length = 60,
                exercise = exercise
            )
        )
        exercise.addSignals(signals)
        return exercise
    }
}
