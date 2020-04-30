package com.epam.brn.integration

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Series
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.TaskRepository
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
@WithMockUser(username = "admin", roles = ["ADMIN"])
class TaskControllerIT {

    @Autowired
    lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    lateinit var taskRepository: TaskRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    lateinit var exercise: Exercise

    @BeforeEach
    fun setUp() {
        val exerciseGroup = insertExerciseGroup()
        val series = insertSeries(exerciseGroup)
        exercise = insertExercise(series)
    }

    @AfterEach
    fun tearDown() {
        exerciseRepository.deleteAll()
        seriesRepository.deleteAll()
        exerciseGroupRepository.deleteAll()
    }

    @Test
    fun `get task by id`() {
        val task = insertTask(exercise)

        // WHEN
        val resultAction = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/tasks/${task.id}")
                    .secure(false)
                    .contentType(MediaType.APPLICATION_JSON)
            )

        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))

        val actual = JSONObject(resultAction.andReturn().response.contentAsString)
            .getJSONObject("data")

        Assertions.assertEquals(task.name, actual.get("name"))
        Assertions.assertEquals(task.id, actual.getLong("id"))
    }

    @Test
    fun `get tasks by exerciseId`() {
        val task = insertTask(exercise)

        // WHEN
        val resultAction = mockMvc
            .perform(
                MockMvcRequestBuilders.get("/tasks")
                    .param("exerciseId", exercise.id.toString())
                    .contentType(MediaType.APPLICATION_JSON)
            )

        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))

        val data = JSONObject(resultAction.andReturn().response.contentAsString)
            .getJSONArray("data")
        Assertions.assertEquals(1, data.length())

        val actual = data.getJSONObject(0)

        Assertions.assertEquals(task.name, actual.get("name"))
        Assertions.assertEquals(task.id, actual.getLong("id"))
    }

    private fun insertExerciseGroup(): ExerciseGroup {
        return exerciseGroupRepository.save(
            ExerciseGroup(
                id = 1,
                description = "desc",
                name = "group"
            )
        )
    }

    private fun insertSeries(exerciseGroup: ExerciseGroup): Series {
        return seriesRepository.save(
            Series(
                id = 1,
                description = "desc",
                name = "series",
                exerciseGroup = exerciseGroup
            )
        )
    }

    fun insertExercise(series: Series): Exercise {
        return exerciseRepository.save(
            Exercise(
                id = 1,
                description = toString(),
                series = series,
                level = 0,
                name = "exercise",
                exerciseType = ExerciseType.WORDS_SEQUENCES.toString()
            )
        )
    }

    private fun insertTask(exercise: Exercise): Task {
        return taskRepository.save(
            Task(
                id = 1,
                name = "${exercise.name} Task",
                serialNumber = 1,
                exercise = exercise
            )
        )
    }
}
