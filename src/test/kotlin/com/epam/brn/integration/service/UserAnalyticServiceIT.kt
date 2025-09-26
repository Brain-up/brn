package com.epam.brn.integration.service

import com.epam.brn.enums.BrnGender
import com.epam.brn.enums.ExerciseType
import com.epam.brn.integration.BaseIT
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.SubGroup
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.UserAccountRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.random.Random

class UserAnalyticServiceIT : BaseIT() {
    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    lateinit var subGroupRepository: SubGroupRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @AfterEach
    fun deleteAfterTest() {
        studyHistoryRepository.deleteAll()
        exerciseRepository.deleteAll()
        subGroupRepository.deleteAll()
        seriesRepository.deleteAll()
        exerciseGroupRepository.deleteAll()
        userAccountRepository.deleteAll()
    }

    @Test
    fun `test repo get last study history for user`() {
        // GIVEN
        val user = insertUser()
        val exerciseFirstName = "exerciseFirstName"
        val exerciseSecondName = "exerciseSecondName"
        val series = insertSeries()
        val subGroup = insertSubGroup(series)
        val exerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val exerciseSecond = insertExercise(exerciseSecondName, subGroup)
        val now = LocalDateTime.now()
        val historyFirstExerciseOne = insertStudyHistory(user, exerciseFirst, now.minusHours(1))
        val historyFirstExerciseTwo = insertStudyHistory(user, exerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(user, exerciseSecond, now.minusHours(1))
        val historySecondExerciseTwo = insertStudyHistory(user, exerciseSecond, now)
        studyHistoryRepository
            .saveAll(
                listOf(
                    historyFirstExerciseOne,
                    historyFirstExerciseTwo,
                    historySecondExerciseOne,
                    historySecondExerciseTwo,
                ),
            )
        // WHEN
        val lastStudyHistoryFirstExercise = studyHistoryRepository.findLastByUserAccountIdAndExerciseId(user.id!!, exerciseFirst.id!!)
        val resultType = exerciseRepository.findTypeByExerciseId(exerciseFirst.id!!)
        // THEN
        lastStudyHistoryFirstExercise!!.id shouldBe historyFirstExerciseTwo.id
        resultType shouldBe ExerciseType.SINGLE_SIMPLE_WORDS.name
    }

    private fun insertStudyHistory(
        existingUser: UserAccount,
        existingExercise: Exercise,
        startTime: LocalDateTime,
    ): StudyHistory =
        studyHistoryRepository.save(
            StudyHistory(
                userAccount = existingUser,
                exercise = existingExercise,
                endTime = startTime.plusMinutes(Random.nextLong(1, 5)),
                startTime = startTime,
                executionSeconds = 122,
                tasksCount = 12,
                wrongAnswers = 2,
                replaysCount = 4,
            ),
        )

    private fun insertUser(): UserAccount =
        userAccountRepository.save(
            UserAccount(
                fullName = "testUserFirstName",
                gender = BrnGender.MALE.toString(),
                bornYear = 2000,
                email = "test@test.test",
                active = true,
            ),
        )

    private fun insertSeries(): Series {
        val exerciseGroup =
            exerciseGroupRepository.save(
                ExerciseGroup(
                    code = "CODE",
                    description = "desc",
                    name = "group",
                ),
            )
        return seriesRepository.save(
            Series(
                description = "desc",
                name = "series",
                exerciseGroup = exerciseGroup,
                level = 1,
                type = ExerciseType.SINGLE_SIMPLE_WORDS.name,
            ),
        )
    }

    private fun insertSubGroup(series: Series): SubGroup =
        subGroupRepository.save(
            SubGroup(series = series, level = 1, code = "code", name = "subGroup name"),
        )

    fun insertExercise(
        exerciseName: String,
        subGroup: SubGroup,
    ): Exercise =
        exerciseRepository.save(
            Exercise(
                subGroup = subGroup,
                level = 0,
                name = exerciseName,
            ),
        )
}
