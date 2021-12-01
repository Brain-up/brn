package com.epam.brn.integration

import com.epam.brn.model.Authority
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Gender
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.SubGroup
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.UserAccountRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.amshove.kluent.internal.platformClassName
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

    @Autowired
    private lateinit var subGroupRepository: SubGroupRepository

    @Autowired
    private lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    private lateinit var seriesRepository: SeriesRepository

    @Autowired
    private lateinit var authorityRepository: AuthorityRepository

    protected val dateFormat = DateTimeFormatter.ISO_DATE_TIME

    /**
     * Should delete data from repositories.
     * Deleting order is matter
     */
    fun deleteInsertedTestData() {
        studyHistoryRepository.deleteAll()
        exerciseGroupRepository.deleteAll()
        exerciseRepository.deleteAll()
        userAccountRepository.deleteAll()
    }

    fun insertDefaultUser(): UserAccount = createUser(fullName = "testUserFirstName", email = "test@test.test")

    fun insertDefaultStudyHistory(
        userAccount: UserAccount,
        exercise: Exercise,
        time: LocalDateTime? = null,
        trainFor: Long = 5
    ): StudyHistory {
        val startTime = time ?: LocalDateTime.now()
        return studyHistoryRepository.save(
            StudyHistory(
                userAccount = userAccount,
                startTime = startTime,
                endTime = startTime.plusMinutes(trainFor),
                executionSeconds = ChronoUnit.SECONDS.between(startTime, startTime.plusMinutes(trainFor)).toInt(),
                exercise = exercise,
                tasksCount = 5,
                wrongAnswers = 0,
                replaysCount = 1
            )
        )
    }

    fun insertDefaultExerciseWithSubGroup(subGroup: SubGroup): Exercise =
        exerciseRepository.save(
            Exercise(
                name = "Test exercise ${subGroup.id}",
                subGroup = subGroup
            )
        )

    fun insertDefaultExercise(subGroup: SubGroup? = null): Exercise =
        exerciseRepository.save(
            Exercise(
                name = "Test exercise",
                subGroup = subGroup
            )
        )

    fun insertDefaultSubGroup(series: Series, level: Int): SubGroup =
        subGroupRepository.save(
            SubGroup(
                series = series,
                level = level,
                code = "code",
                name = "subGroupName$level"
            )
        )

    fun insertDefaultSeries(): Series =
        seriesRepository.save(
            Series(
                name = "Series for ${platformClassName()}",
                exerciseGroup = insertDefaultExerciseGroup(),
                type = "Type",
                level = 1
            )
        )

    fun insertDefaultExerciseGroup(): ExerciseGroup =
        exerciseGroupRepository.save(
            ExerciseGroup(
                code = "CODE",
                description = "Description",
                name = "Test exercise group for ${platformClassName()}"
            )
        )

    fun createUser(
        fullName: String? = null,
        email: String,
        active: Boolean = true,
        bornYear: Int = 2000,
        gender: String = Gender.FEMALE.toString(),
        authorities: MutableSet<Authority> = mutableSetOf()
    ): UserAccount {
        return userAccountRepository.save(
            UserAccount(
                fullName = fullName ?: email,
                email = email,
                active = active,
                bornYear = bornYear,
                gender = gender
            ).apply { authorities.isNotEmpty().let { authoritySet.addAll(authorities) } }
        )
    }

    fun createAuthority(authorityName: String): Authority =
        authorityRepository.findAuthorityByAuthorityName(authorityName)
            ?: authorityRepository.save(Authority(authorityName = authorityName))
}
