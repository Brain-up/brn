package com.epam.brn.integration

import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.TaskRepository
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.enums.ExerciseType
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class TaskControllerIT : BaseIT() {

    @Autowired
    lateinit var taskRepository: TaskRepository

    @Autowired
    lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    lateinit var subGroupRepository: SubGroupRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    lateinit var exercise: Exercise

    @BeforeEach
    fun setUp() {
        val exerciseGroup = insertExerciseGroup()
        val series = insertSeries(exerciseGroup)
        val subGroup = insertSubGroup(series)
        exercise = insertExercise(subGroup)
    }

    @AfterEach
    fun tearDown() {
        exerciseRepository.deleteAll()
        subGroupRepository.deleteAll()
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
                exerciseGroup = exerciseGroup,
                level = 1,
                type = ExerciseType.SINGLE_SIMPLE_WORDS.name
            )
        )
    }

    private fun insertSubGroup(series: Series): SubGroup = subGroupRepository.save(
        SubGroup(series = series, level = 1, code = "code", name = "subGroup name")
    )

    private fun insertExercise(subGroup: SubGroup): Exercise {
        return exerciseRepository.save(
            Exercise(
                id = 1,
                subGroup = subGroup,
                level = 0,
                name = "exercise"
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
