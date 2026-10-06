package com.epam.brn.integration.repo

import com.epam.brn.enums.ExerciseType
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.SeriesRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterAll
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
class SeriesRepositoryTest {
    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    @AfterAll
    fun deleteAfterTest() {
        exerciseGroupRepository.deleteAll()
        seriesRepository.deleteAll()
    }

    @Test
    fun `findByIdAndActiveTrue should return correct data`() {
        // GIVEN
        val group = insertGroup()
        val series1 = insertSeries(group, "series one", true)
        val series2 = insertSeries(group, "series two", false)

        // WHEN
        val result = seriesRepository.findByIdAndActiveTrue(series1.id!!)

        // THEN
        result?.active shouldBe true
        result?.id shouldNotBe series2.id!!
    }

    @Test
    fun `findByExerciseGroupIdWithSubGroups should return correct data`() {
        // GIVEN
        val group = insertGroup()
        val series1 = insertSeries(group, "series one", true)
        val series2 = insertSeries(group, "series two", false)

        // WHEN
        val result = seriesRepository.findByExerciseGroupIdWithSubGroups(group.id!!)

        // THEN
        result.shouldNotBeNull()
        result shouldHaveSize 1
        result[0].active shouldBe true
        result[0].id shouldBe series1.id!!
        result[0].id shouldNotBe series2.id!!
    }

    private fun insertGroup(): ExerciseGroup {
        val group =
            ExerciseGroup(code = "CODE", name = "GroupName", description = "GroupDescription SeriesControllerIT")
        return exerciseGroupRepository.save(group)
    }

    private fun insertSeries(
        group: ExerciseGroup,
        name: String,
        active: Boolean,
    ): Series {
        val series =
            Series(
                name = name,
                description = "description",
                exerciseGroup = group,
                level = 1,
                type = ExerciseType.SINGLE_SIMPLE_WORDS.name,
                active = active,
            )
        return seriesRepository.save(series)
    }
}
