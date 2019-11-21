package com.epam.brn.service

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseGroupRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
@Profile("dev", "prod")
class LoadHandBook(private val exerciseGroupRepository: ExerciseGroupRepository) {

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationEvent(event: ApplicationReadyEvent) {
        loadInitialDataToDb()
    }

    fun loadInitialDataToDb() {
        val resource11 =
            Resource(audioFileUrl = "no_noise/бал.mp3", word = "бал", pictureFileUrl = "", soundsCount = 1)
        val resource12 =
            Resource(audioFileUrl = "no_noise/бум.mp3", word = "бум", pictureFileUrl = "", soundsCount = 1)
        val resource13 =
            Resource(audioFileUrl = "no_noise/быль.mp3", word = "быль", pictureFileUrl = "", soundsCount = 1)
        val resource14 =
            Resource(audioFileUrl = "no_noise/вить.mp3", word = "вить", pictureFileUrl = "", soundsCount = 1)
        val resource15 =
            Resource(audioFileUrl = "no_noise/гад.mp3", word = "гад", pictureFileUrl = "", soundsCount = 1)
        val resource16 =
            Resource(audioFileUrl = "", word = "сад", pictureFileUrl = "", soundsCount = 1)

        val resource21 =
            Resource(audioFileUrl = "noise_0db/бал.mp3", word = "бал", pictureFileUrl = "", soundsCount = 1)
        val resource22 =
            Resource(audioFileUrl = "noise_0db/бум.mp3", word = "бум", pictureFileUrl = "", soundsCount = 1)
        val resource23 =
            Resource(audioFileUrl = "noise_0db/быль.mp3", word = "быль", pictureFileUrl = "", soundsCount = 1)
        val resource24 =
            Resource(audioFileUrl = "noise_0db/вить.mp3", word = "вить", pictureFileUrl = "", soundsCount = 1)
        val resource25 =
            Resource(audioFileUrl = "noise_0db/гад.mp3", word = "гад", pictureFileUrl = "", soundsCount = 1)
        val resource26 =
            Resource(audioFileUrl = "", word = "сад", pictureFileUrl = "", soundsCount = 1)

        val group1 = ExerciseGroup(name = "Неречевые упражения", description = "неречевые упражения")
        val group2 = ExerciseGroup(name = "Речевые упражения", description = "речевые упражения")

        val series1 =
            Series(name = "Распознование слов", description = "распознование слов", exerciseGroup = group2)
        val series2 =
            Series(name = "Диахоничкеское слушание", description = "диахоничкеское слушание", exerciseGroup = group2)

        // ============ exercise ==========
        val exercise1 = Exercise(
            name = "Однослоговые слова без шума",
            description = "Однослоговые слова без шума",
            level = 0,
            series = series1
        )
        val exercise2 = Exercise(
            name = "Однослоговые слова без шума",
            description = "Однослоговые слова без шума",
            level = 1,
            series = series1
        )
        val exercise3 = Exercise(
            name = "Однослоговые слова без шума",
            description = "Однослоговые слова без шума",
            level = 2,
            series = series1
        )

        val exercise4 = Exercise(
            name = "Однослоговые слова c малым шумом",
            description = "Однослоговые слова c малым шумом",
            level = 4,
            series = series1
        )
        val exercise5 = Exercise(
            name = "Однослоговые слова c малым шумом",
            description = "Однослоговые слова c малым шумом",
            level = 5,
            series = series1
        )
        val exercise6 = Exercise(
            name = "Однослоговые слова c малым шумом",
            description = "Однослоговые слова c малым шумом",
            level = 6,
            series = series1
        )

        val exercise7 = Exercise(
            name = "Однослоговые слова c сильным шумом",
            description = "Однослоговые слова c сильным шумом",
            level = 7,
            series = series1
        )
        val exercise8 = Exercise(
            name = "Однослоговые слова c сильным шумом",
            description = "Однослоговые слова c сильным шумом",
            level = 8,
            series = series1
        )
        val exercise9 = Exercise(
            name = "Однослоговые слова",
            description = "Однослоговые слова c сильным шумом",
            level = 9,
            series = series1
        )

        val task11 = Task(
            name = "task 1",
            serialNumber = 1,
            exercise = exercise1,
            correctAnswer = resource11
        )
        task11.answerOptions.addAll(setOf(resource12, resource13, resource14, resource15, resource16))
        val task12 = Task(
            name = "task 2",
            serialNumber = 2,
            exercise = exercise1,
            correctAnswer = resource12
        )
        task12.answerOptions.addAll(setOf(resource11, resource13, resource14, resource15, resource16))
        exercise1.tasks.addAll(listOf(task11, task12))

        series1.exercises.addAll(linkedSetOf(exercise1, exercise2, exercise3, exercise4, exercise5, exercise6, exercise7, exercise8, exercise9))

        group2.series.addAll(listOf(series1, series2))
        exerciseGroupRepository.saveAll(listOf(group1, group2))
    }
}
