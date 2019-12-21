package com.epam.brn.integration

import com.epam.brn.constant.BrnPath
import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseGroupRepository
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
class TaskControllerIT {

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository
    @Autowired
    lateinit var mockMvc: MockMvc

    lateinit var firstSavedTask: Task
    lateinit var secondSavedTask: Task

    lateinit var savedExercise: Exercise

    @BeforeEach
    fun initBeforeEachTest() {
        val group = ExerciseGroup(name = "речевые упражения", description = "речевые упражения")
        val series1 =
            Series(name = "распознование слов", description = "распознование слов", exerciseGroup = group)
        val series2 = Series(
            name = "диахоничкеское слушание",
            description = "диахоничкеское слушание",
            exerciseGroup = group
        )
        group.series.addAll(setOf(series1, series2))
        savedExercise = Exercise(name = "First", description = "desc", level = 0, series = series1, exerciseType = ExerciseTypeEnum.SINGLE_WORDS.toString())
        val secondExercise = Exercise(name = "Second", description = "desc", level = 0, series = series1, exerciseType = ExerciseTypeEnum.SINGLE_WORDS.toString())
        series1.exercises.addAll(listOf(savedExercise, secondExercise))
        firstSavedTask = Task(
            name = "firstTaskForExercise",
            serialNumber = 1,
            exercise = savedExercise
        )
        secondSavedTask = Task(
            name = "secondTaskForSecondExercise",
            serialNumber = 2,
            exercise = secondExercise
        )
        savedExercise.tasks.add(firstSavedTask)
        secondExercise.tasks.add(secondSavedTask)
        exerciseGroupRepository.save(group)
    }

    @AfterEach
    fun deleteAfterTest() {
        exerciseGroupRepository.deleteAll()
    }

    @Test
    fun `test get task by id`() {
        // WHEN
        val pathInfo = "/${firstSavedTask.id}"
        val resultAction = mockMvc.perform(
            get(BrnPath.TASKS + pathInfo)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        val jsonResponse = JSONObject(resultAction.andReturn().response.contentAsString)
        val jsonDataObject = jsonResponse.getJSONObject("data")
        assertEquals(firstSavedTask.name, jsonDataObject.get("name"))
        assertEquals(firstSavedTask.id, jsonDataObject.getLong("id"))
    }

    @Test
    fun `test tasks by exerciseId`() {
        // WHEN
        val resultAction = mockMvc.perform(
            get(BrnPath.TASKS)
                .param("exerciseId", savedExercise.id.toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )

        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
        val jsonResponse = JSONObject(resultAction.andReturn().response.contentAsString)
        val jsonDataObject = jsonResponse.getJSONArray("data").getJSONObject(0)
        assertEquals(firstSavedTask.name, jsonDataObject.get("name"))
        assertEquals(firstSavedTask.id, jsonDataObject.getLong("id"))
        assertEquals(1, jsonResponse.getJSONArray("data").length())
    }
}