package com.epam.brn.integration.repo

import com.epam.brn.repo.TaskRepository
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE
import org.apache.commons.lang3.math.NumberUtils.INTEGER_TWO
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
@Tag("integration-test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
                exerciseId?.let { taskRepository.findTasksByExerciseIdWithJoinedAnswers(it) }

            // THEN
            findAllTasksWithAnswers.shouldNotBeNull()
            findAllTasksWithAnswers shouldHaveSize INTEGER_ONE
            findAllTasksWithAnswers.single().name shouldBe nameOfTaskWithAnswers
        }

        @Test
        fun `should return task by id`() {
            // WHEN
            val resultedTask =
                savedTasked?.id?.let { taskRepository.findById(it) }

            // THEN
            resultedTask.shouldNotBeNull()
            resultedTask.isPresent shouldBe true
            val actualTask = resultedTask.get()
            actualTask.id shouldBe savedTasked?.id
            actualTask.name shouldBe savedTasked?.name
            actualTask.serialNumber shouldBe savedTasked?.serialNumber
        }

        @Test
        fun `should return all tasks`() {
            // WHEN
            val findAllTasksWithAnswers = taskRepository.findAllTasksWithJoinedAnswers()

            // THEN
            findAllTasksWithAnswers shouldHaveSize INTEGER_TWO
        }

        @Test
        fun `should return all tasks include answers`() {
            // WHEN
            val findAllTasksWithAnswers = taskRepository.findAllTasksWithJoinedAnswers()
            val actualListOfWords =
                findAllTasksWithAnswers
                    .filter { task -> task.name.equals(nameOfTaskWithAnswers) }
                    .map { task -> task.answerOptions }
                    .flatten()
                    .map { resource -> resource.word }

            // THEN
            actualListOfWords shouldContainExactlyInAnyOrder listOfWords
        }
    }
}
