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
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.then
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class SignalSeriesRecordProcessorTest {
    private val exerciseRepositoryMock = mock(ExerciseRepository::class.java)
    private val subGroupRepositoryMock = mock(SubGroupRepository::class.java)

    private lateinit var signalSeriesRecordProcessor: SignalSeriesRecordProcessor

    private val exerciseGroup = ExerciseGroup(
        id = 1L,
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
        given(subGroupRepositoryMock.findByCodeAndLocale("durationSignals", locale))
            .willReturn(subGroupDuration)
    }

    @Test
    fun `should create correct exercise`() {
        val exercise = createExercise(subGroupDuration)
        given(exerciseRepositoryMock.save(exercise))
            .willReturn(exercise)
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

        then(subGroupRepositoryMock)
            .should()
            .findByCodeAndLocale("durationSignals", locale)

        Assertions.assertThat(actual).contains(exercise)
        then(exerciseRepositoryMock)
            .should()
            .save(exercise)
    }

    @Test
    fun `should create correct exercise for different series`() {
        // GIVEN
        val ex1 = createExercise(subGroupDuration)
        val ex2 = createExercise(subGroupFrequency)
        `when`(exerciseRepositoryMock.findByNameAndLevel("По 2 сигнала разной длительности.", 1)).thenReturn(null)
        `when`(exerciseRepositoryMock.findByNameAndLevel("По 2 сигнала разной частоты.", 1)).thenReturn(null)
        `when`(exerciseRepositoryMock.save(ex1)).thenReturn(ex1)
        `when`(exerciseRepositoryMock.save(ex2)).thenReturn(ex2)
        `when`(subGroupRepositoryMock.findByCodeAndLocale("subGroupDuration", locale)).thenReturn(subGroupDuration)
        `when`(subGroupRepositoryMock.findByCodeAndLocale("subGroupFrequency", locale)).thenReturn(subGroupFrequency)
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
        then(exerciseRepositoryMock).should().save(ex1)
        then(exerciseRepositoryMock).should().save(ex2)
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
        `when`(subGroupRepositoryMock.findByCodeAndLocale(code, locale.locale)).thenReturn(subGroupDuration)

        val exercise = createExerciseWithSignals(subGroupDuration)
        `when`(exerciseRepositoryMock.save(exercise)).thenReturn(exercise)
        `when`(exerciseRepositoryMock.findByNameAndLevel("По 2 сигнала разной длительности.", 1)).thenReturn(null)
        // WHEN
        val actual = signalSeriesRecordProcessor.process(listOf(record), locale)
        // THEN
        Assertions.assertThat(actual).contains(exercise)
        then(exerciseRepositoryMock).should().save(exercise)
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
