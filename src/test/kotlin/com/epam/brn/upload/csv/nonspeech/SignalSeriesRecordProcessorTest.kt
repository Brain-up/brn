package com.epam.brn.upload.csv.nonspeech

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.enums.ExerciseType
import com.epam.brn.model.Series
import com.epam.brn.model.Signal
import com.epam.brn.model.SubGroup
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.then
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class SignalSeriesRecordProcessorTest {
    private val exerciseRepositoryMock = mock(ExerciseRepository::class.java)
    private val subGroupRepositoryMock = mock(SubGroupRepository::class.java)
    private val seriesRepositoryMock = mock(SeriesRepository::class.java)

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

    @BeforeEach
    internal fun setUp() {
        signalSeriesRecordProcessor = SignalSeriesRecordProcessor(
            subGroupRepositoryMock,
            exerciseRepositoryMock
        )
        given(subGroupRepositoryMock.findByCode("durationSignals"))
            .willReturn(subGroupDuration)
    }

    @Test
    fun `should create correct exercise`() {
        val expected = listOf(createExercise(subGroupDuration))
        given(exerciseRepositoryMock.saveAll(expected))
            .willReturn(expected)
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
            .findByCode("durationSignals")

        Assertions.assertThat(actual).isEqualTo(expected)
        then(exerciseRepositoryMock)
            .should()
            .saveAll(expected)
    }

    @Test
    fun `should create correct exercise for different series`() {
        val expected = listOf(createExercise(null), createExercise(null))
        given(exerciseRepositoryMock.saveAll(expected))
            .willReturn(expected)
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
                    exerciseName = "По 2 сигнала разной длительности.",
                    exerciseType = ExerciseType.FREQUENCY_SIGNALS,
                    level = 1,
                    code = "subGroupFrequency",
                    signals = emptyList()
                )
            )
        )

        then(subGroupRepositoryMock)
            .should()
            .findByCode("subGroupDuration")
        then(subGroupRepositoryMock)
            .should()
            .findByCode("subGroupFrequency")

        Assertions.assertThat(actual).isEqualTo(expected)
        then(exerciseRepositoryMock)
            .should()
            .saveAll(expected)
    }

    @Test
    fun `should create correct signals`() {
        val expected = listOf(createExerciseWithSignals(null))
        given(exerciseRepositoryMock.saveAll(expected))
            .willReturn(expected)
        val actual = signalSeriesRecordProcessor.process(
            listOf(
                SignalSeriesRecord(
                    exerciseName = "По 2 сигнала разной длительности.",
                    exerciseType = ExerciseType.DURATION_SIGNALS,
                    level = 1,
                    code = "subGroupDuration",
                    signals = listOf("1000 120", "1000 60")
                )
            )
        )

        then(subGroupRepositoryMock)
            .should()
            .findByCode("subGroupDuration")

        Assertions.assertThat(actual).isEqualTo(expected)
        then(exerciseRepositoryMock)
            .should()
            .saveAll(expected)
    }

    private fun createExercise(subGroup: SubGroup?): Exercise {
        return Exercise(
            subGroup = subGroup,
            name = "По 2 сигнала разной длительности.",
            level = 1
        )
    }

    private fun createExerciseWithSignals(subGroup: SubGroup?): Exercise {
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
