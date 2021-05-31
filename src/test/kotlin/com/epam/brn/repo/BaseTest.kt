package com.epam.brn.repo

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
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
        val group = ExerciseGroup(code = "CODE", name = "речевые упражнения", description = "речевые упражнения")
        val series = Series(
            name = "распознавание слов",
            description = "распознавание слов",
            exerciseGroup = group,
            level = 1,
            type = "type"
        )
        group.series.addAll(setOf(series))
        val subGroup1 = SubGroup(series = series, level = 1, code = "code1", name = "subGroup name1")
        val subGroup2 = SubGroup(series = series, level = 2, code = "code2", name = "subGroup name2")
        series.subGroups.addAll(listOf(subGroup1, subGroup2))

        val exercise1 = Exercise(
            name = "First",
            level = 0,
            subGroup = subGroup1
        )
        val exercise2 = Exercise(
            name = "Second",
            level = 0,
            subGroup = subGroup2
        )
        subGroup1.exercises.addAll(listOf(exercise1, exercise2))

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
            exercise = exercise1,
            correctAnswer = firstResource
        )
        val secondTask = Task(
            name = "secondTask",
            serialNumber = 1,
            exercise = exercise2,
            correctAnswer = firstResource
        )
        task.answerOptions.addAll(setOf(firstResource, secondResource, thirdResource))
        exercise1.tasks.addAll(listOf(task, secondTask))
        exerciseGroupRepository.save(group)
        exerciseId = exercise1.id
        savedTasked = task
    }
}
