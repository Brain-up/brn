package com.epam.brn.integration

import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.BrnRole
import com.epam.brn.enums.ExerciseType
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.TaskRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertFalse

@WithMockUser(username = "test@test.test", roles = [BrnRole.USER])
class GroupControllerIT : BaseIT() {

    private val baseUrl = "/groups"

    @Autowired
    private lateinit var exerciseGroupRepository: ExerciseGroupRepository
    @Autowired
    private lateinit var seriesRepository: SeriesRepository
    @Autowired
    private lateinit var subGroupRepository: SubGroupRepository
    @Autowired
    private lateinit var exerciseRepository: ExerciseRepository
    @Autowired
    private lateinit var taskRepository: TaskRepository

    @AfterEach
    fun deleteAfterTest() {
        exerciseGroupRepository.deleteAll()
    }

    @Test
    fun `test find ru groups`() {
        // GIVEN
        val exerciseGroupName1 = "GroupName1"
        val groupRu = insertExerciseGroup(exerciseGroupName1, BrnLocale.RU.locale)
        val exerciseGroupName2 = "GroupName2"
        val groupEn = insertExerciseGroup(exerciseGroupName2, BrnLocale.EN.locale)
        val series1 = insertSeries(groupRu, "series1Name")
        val series2 = insertSeries(groupRu, "series2Name")
        val subGroup1 = insertSubGroup(series1, 1)
        val subGroup2 = insertSubGroup(series1, 2)
        val exercise = insertExercise(subGroup1, "ex1")
        val task = insertTask(exercise)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl)
                .param("locale", BrnLocale.RU.locale)
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.contentAsString
        assertTrue(response.contains(groupRu.name))
        assertFalse(response.contains(groupEn.name))
    }

    @Test
    fun `test get group by Id`() {
        // GIVEN
        val exerciseGroupName = "GroupName"
        val existingExerciseGroup = insertExerciseGroup(exerciseGroupName)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl + "/" + existingExerciseGroup.id)
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.contentAsString
        assertTrue(response.contains(existingExerciseGroup.name))
    }

    fun insertExerciseGroup(exerciseGroupName: String, locale: String = BrnLocale.RU.locale): ExerciseGroup =
        exerciseGroupRepository.save(
            ExerciseGroup(
                code = "CODE",
                description = "desc",
                name = exerciseGroupName,
                locale = locale
            )
        )

    fun insertSeries(group: ExerciseGroup, name: String): Series {
        val series = Series(
            name = name,
            description = "description",
            exerciseGroup = group,
            level = 1,
            type = ExerciseType.SINGLE_SIMPLE_WORDS.name
        )
        return seriesRepository.save(series)
    }

    fun insertSubGroup(series: Series, level: Int): SubGroup =
        subGroupRepository.save(
            SubGroup(
                series = series,
                level = level,
                code = "code",
                name = "${series.name}subGroupName$level"
            )
        )

    fun insertExercise(subGroup: SubGroup, exerciseName: String): Exercise =
        exerciseRepository.save(
            Exercise(
                subGroup = subGroup,
                level = 0,
                name = exerciseName,
                noiseLevel = 50,
                noiseUrl = "/testNoiseUrl"
            )
        )

    fun insertTask(exercise: Exercise): Task =
        taskRepository.save(
            Task(
                id = 1,
                name = "${exercise.name} Task",
                serialNumber = 1,
                exercise = exercise
            )
        )
}
