package com.epam.brn.upload.csv.nonspeech

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Series
import com.epam.brn.model.Signal
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
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
    private val seriesRepositoryMock = mock(SeriesRepository::class.java)

    private lateinit var signalSeriesRecordProcessor: SignalSeriesRecordProcessor

    private val exerciseGroup = ExerciseGroup(
        id = 1L,
        name = "Неречевые упражнения",
        description = "Неречевые упражнения"
    )

    private val lengthSeries = Series(
        id = 11L,
        name = "Длительность сигналов",
        description = "Длительность сигналов",
        exerciseGroup = exerciseGroup
    )

    private val frequencySeries = Series(
        id = 12L,
        name = "Частота сигналов",
        description = "Частота сигналов",
        exerciseGroup = exerciseGroup
    )

    private val seriesList = listOf(lengthSeries, frequencySeries)

    @BeforeEach
    internal fun setUp() {
        signalSeriesRecordProcessor = SignalSeriesRecordProcessor(
            exerciseRepositoryMock,
            seriesRepositoryMock
        )
        seriesList.forEach {
            given(seriesRepositoryMock.findByNameIn(listOf(it.name)))
                .willReturn(listOf(it))
        }
        given(seriesRepositoryMock.findByNameIn(seriesList.map { it.name }))
            .willReturn(seriesList)
    }

    @Test
    fun `should create correct exercise`() {
        val expected = listOf(createExercise(frequencySeries))
        given(exerciseRepositoryMock.saveAll(expected))
            .willReturn(expected)
        val actual = signalSeriesRecordProcessor.process(
            listOf(
                SignalSeriesRecord(
                    exerciseName = "По 2 сигнала разной длительности.",
                    exerciseType = ExerciseType.TWO_DIFFERENT_LENGTH_SIGNAL,
                    level = 1,
                    series = frequencySeries.name,
                    signals = emptyList()
                )
            )
        )

        then(seriesRepositoryMock)
            .should()
            .findByNameIn(listOf(frequencySeries.name))

        Assertions.assertThat(actual).isEqualTo(expected)
        then(exerciseRepositoryMock)
            .should()
            .saveAll(expected)
    }

    @Test
    fun `should create correct exercise for different series`() {
        val expected = listOf(createExercise(lengthSeries), createExercise(frequencySeries))
        given(exerciseRepositoryMock.saveAll(expected))
            .willReturn(expected)
        val actual = signalSeriesRecordProcessor.process(
            listOf(
                SignalSeriesRecord(
                    exerciseName = "По 2 сигнала разной длительности.",
                    exerciseType = ExerciseType.TWO_DIFFERENT_LENGTH_SIGNAL,
                    level = 1,
                    series = lengthSeries.name,
                    signals = emptyList()
                ),
                SignalSeriesRecord(
                    exerciseName = "По 2 сигнала разной длительности.",
                    exerciseType = ExerciseType.TWO_DIFFERENT_LENGTH_SIGNAL,
                    level = 1,
                    series = frequencySeries.name,
                    signals = emptyList()
                )
            )
        )

        then(seriesRepositoryMock)
            .should()
            .findByNameIn(listOf(lengthSeries.name, frequencySeries.name))

        Assertions.assertThat(actual).isEqualTo(expected)
        then(exerciseRepositoryMock)
            .should()
            .saveAll(expected)
    }

    @Test
    fun `should create correct signals`() {
        val expected = listOf(createExerciseWithSignals(frequencySeries))
        given(exerciseRepositoryMock.saveAll(expected))
            .willReturn(expected)
        val actual = signalSeriesRecordProcessor.process(
            listOf(
                SignalSeriesRecord(
                    exerciseName = "По 2 сигнала разной длительности.",
                    exerciseType = ExerciseType.TWO_DIFFERENT_LENGTH_SIGNAL,
                    level = 1,
                    series = frequencySeries.name,
                    signals = listOf("1000 120", "1000 60")
                )
            )
        )

        then(seriesRepositoryMock)
            .should()
            .findByNameIn(listOf(frequencySeries.name))

        Assertions.assertThat(actual).isEqualTo(expected)
        then(exerciseRepositoryMock)
            .should()
            .saveAll(expected)
    }

    private fun createExercise(series: Series): Exercise {
        return Exercise(
            series = series,
            name = "По 2 сигнала разной длительности.",
            exerciseType = ExerciseType.TWO_DIFFERENT_LENGTH_SIGNAL.toString(),
            level = 1
        )
    }

    private fun createExerciseWithSignals(series: Series): Exercise {
        val exercise = Exercise(
            series = series,
            name = "По 2 сигнала разной длительности.",
            exerciseType = ExerciseType.TWO_DIFFERENT_LENGTH_SIGNAL.toString(),
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
