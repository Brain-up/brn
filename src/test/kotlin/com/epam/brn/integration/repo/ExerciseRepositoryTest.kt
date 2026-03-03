package com.epam.brn.integration.repo

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.Signal
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.SessionFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestPropertySource
import javax.persistence.EntityManagerFactory

@DataJpaTest
@Tag("integration-test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = ["spring.jpa.properties.hibernate.generate_statistics=true"])
class ExerciseRepositoryTest {
    @Autowired
    lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var resourceRepository: ResourceRepository

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    @AfterEach
    fun deleteAfterTest() {
        exerciseGroupRepository.deleteAll()
        resourceRepository.deleteAll()
    }

    @Test
    fun `findExercisesWithSubGroupBySubGroupId should load dto graph without extra sql`() {
        val subGroupId = insertExerciseGraph()
        val sessionFactory = entityManagerFactory.unwrap(SessionFactory::class.java)
        val statistics = sessionFactory.statistics
        statistics.clear()

        val exercises = exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId)
        val preparedStatementsAfterFetch = statistics.prepareStatementCount
        val persistenceUnitUtil = entityManagerFactory.persistenceUnitUtil

        assertThat(exercises).hasSize(1)

        val exercise = exercises.single()
        val subGroup = exercise.subGroup!!
        val tasks = exercise.tasks.toList()
        val signals = exercise.signals.toList()
        val answerWords =
            tasks
                .flatMap { task -> task.answerOptions.toList() }
                .map { resource -> resource.word }

        assertThat(exercise.name).isEqualTo("Exercise")
        assertThat(subGroup.series.name).startsWith("Series")
        assertThat(tasks).hasSize(1)
        assertThat(signals).hasSize(1)
        assertThat(answerWords).containsExactlyInAnyOrder("alpha", "beta")

        assertThat(persistenceUnitUtil.isLoaded(exercise, "subGroup")).isTrue()
        assertThat(persistenceUnitUtil.isLoaded(subGroup, "series")).isTrue()
        assertThat(persistenceUnitUtil.isLoaded(exercise, "tasks")).isTrue()
        assertThat(persistenceUnitUtil.isLoaded(tasks.first(), "answerOptions")).isTrue()
        assertThat(persistenceUnitUtil.isLoaded(exercise, "signals")).isTrue()
        assertThat(statistics.prepareStatementCount).isEqualTo(preparedStatementsAfterFetch)
    }

    private fun insertExerciseGraph(): Long {
        val suffix = System.nanoTime()
        val alpha = resourceRepository.save(Resource(word = "alpha", wordType = "OBJECT"))
        val beta = resourceRepository.save(Resource(word = "beta", wordType = "OBJECT"))

        val group =
            ExerciseGroup(
                code = "CODE$suffix",
                name = "Group-$suffix",
                description = "Exercise group for repository test",
            )
        val series =
            Series(
                type = "WORDS",
                name = "Series-$suffix",
                level = 1,
                description = "Series for repository test",
                exerciseGroup = group,
            )
        group.series.add(series)

        val subGroup =
            SubGroup(
                series = series,
                level = 1,
                code = "SUB$suffix",
                name = "SubGroup-$suffix",
                description = "Subgroup for repository test",
            )
        series.subGroups.add(subGroup)

        val exercise =
            Exercise(
                name = "Exercise",
                level = 1,
                subGroup = subGroup,
            )
        subGroup.exercises.add(exercise)

        val task =
            Task(
                name = "Task",
                serialNumber = 1,
                exercise = exercise,
                correctAnswer = alpha,
            )
        task.answerOptions.add(alpha)
        task.answerOptions.add(beta)
        exercise.addTask(task)

        val signal =
            Signal(
                name = "Signal",
                url = "signal-url",
                frequency = 1000,
                length = 500,
                exercise = exercise,
            )
        exercise.addSignals(listOf(signal))

        val savedGroup = exerciseGroupRepository.save(group)
        return savedGroup
            .series
            .single()
            .subGroups
            .single()
            .id!!
    }
}
