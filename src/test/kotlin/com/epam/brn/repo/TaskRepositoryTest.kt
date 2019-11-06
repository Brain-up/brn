package com.epam.brn.repo

import com.epam.brn.model.Task
import org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE
import org.apache.commons.lang3.math.NumberUtils.INTEGER_TWO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("TaskRepository tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskRepositoryTest : BaseTest() {

    @Autowired
    lateinit var taskRepository: TaskRepository

    @Nested
    @DisplayName("Tests for getting tasks using custom queries with parameters")
    inner class GetTasks {
        @Test
        fun `should return list with one task includes answers for certain exercise`() {

            // WHEN
            val findAllTasksWithAnswers =
                exerciseId?.let { taskRepository.findAllTasksByExerciseIdWithJoinedAnswers(it) }

            // THEN
            assertThat(findAllTasksWithAnswers)
                .hasSize(INTEGER_ONE)
                .usingElementComparatorOnFields("name")
                .containsExactly(Task(name = nameOfTaskWithAnswers))
        }

        @Test
        fun `should return task by id`() {
            // WHEN
            val resultedTask =
                savedTasked?.id?.let { taskRepository.findById(it) }

            // THEN
            assertThat(resultedTask)
                .hasValueSatisfying {
                    assertThat(it)
                        .isEqualToComparingOnlyGivenFields(savedTasked, "id", "name", "serialNumber")
                }
        }

        @Test
        fun `should return all tasks`() {

            // WHEN
            val findAllTasksWithAnswers = taskRepository.findAllTasksWithJoinedAnswers()

            // THEN
            assertThat(findAllTasksWithAnswers)
                .hasSize(INTEGER_TWO)
        }

        @Test
        fun `should return all tasks include answers`() {

            // WHEN
            val findAllTasksWithAnswers = taskRepository.findAllTasksWithJoinedAnswers()
            val actualListOfWords = findAllTasksWithAnswers
                .filter { task -> task.name.equals(nameOfTaskWithAnswers) }
                .map { task -> task.answerOptions }
                .flatten()
                .map { resource -> resource.word }

            // THEN
            assertThat(actualListOfWords)
                .containsExactlyInAnyOrderElementsOf(listOfWords)
        }
    }
}