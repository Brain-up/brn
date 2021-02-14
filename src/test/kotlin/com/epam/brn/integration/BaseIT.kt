package com.epam.brn.integration

import com.epam.brn.model.Exercise
import com.epam.brn.model.Gender
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
@Testcontainers
abstract class BaseIT {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    private lateinit var studyHistoryRepository: StudyHistoryRepository

    @Autowired
    private lateinit var exerciseRepository: ExerciseRepository

    fun insertDefaultUser(): UserAccount {
        return userAccountRepository.save(
            UserAccount(
                fullName = "testUserFirstName",
                email = "test@test.test",
                password = "password",
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                active = true
            )
        )
    }

    fun insertStudyHistory(
        userAccount: UserAccount,
        exercise: Exercise,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        tasksCount: Short,
        wrongAnswers: Int,
        replayCount: Int
    ): StudyHistory {
        return studyHistoryRepository.save(
            StudyHistory(
                userAccount = userAccount,
                exercise = exercise,
                startTime = startTime,
                endTime = endTime,
                executionSeconds = Duration.between(startTime, endTime).seconds.toInt(),
                tasksCount = tasksCount,
                wrongAnswers = wrongAnswers,
                replaysCount = replayCount
            )
        )
    }

    fun insertExercise(exercise: Exercise): Exercise {
        return exerciseRepository.save(
            exercise
        )
    }
}
