package com.epam.brn.repo

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired

abstract class BaseTest {

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var resourceRepository: ResourceRepository

    val listOfWords = listOf("son", "lon", "slo")
    val nameOfTaskWithAnswers = "firstTask"
    var exerciseId: Long? = null
    var savedTasked: Task? = null

    @BeforeAll
    fun init() {
        val group = ExerciseGroup(name = "речевые упражнения", description = "речевые упражнения")
        val series1 =
            Series(name = "распознавание слов", description = "распознавание слов", exerciseGroup = group)
        val series2 = Series(
            name = "диахоничкеское слушание",
            description = "диахоничкеское слушание",
            exerciseGroup = group
        )
        group.series.addAll(setOf(series1, series2))
        val exercise = Exercise(name = "First", description = "desc", level = 0, series = series1, exerciseType = ExerciseType.SINGLE_WORDS.toString())
        val secondExercise = Exercise(name = "Second", description = "desc", level = 0, series = series1, exerciseType = ExerciseType.SINGLE_WORDS.toString())
        series1.exercises.addAll(listOf(exercise, secondExercise))

        val firstResource =
            Resource(audioFileUrl = "audio_f", word = listOfWords[0], pictureFileUrl = "picture_f", soundsCount = 0)
        val secondResource =
            Resource(audioFileUrl = "audio_s", word = listOfWords[1], pictureFileUrl = "picture_s", soundsCount = 0)
        val thirdResource =
            Resource(audioFileUrl = "audio_t", word = listOfWords[2], pictureFileUrl = "picture_t", soundsCount = 0)

        resourceRepository.saveAll(listOf(firstResource, secondResource, thirdResource))

        val task = Task(
            name = nameOfTaskWithAnswers,
            serialNumber = 1,
            exercise = exercise,
            correctAnswer = firstResource
        )
        val secondTask = Task(
            name = "secondTask",
            serialNumber = 1,
            exercise = secondExercise,
            correctAnswer = firstResource
        )
        task.answerOptions.addAll(setOf(firstResource, secondResource, thirdResource))
        exercise.tasks.addAll(listOf(task, secondTask))
        exerciseGroupRepository.save(group)
        exerciseId = exercise.id
        savedTasked = task
    }
}
