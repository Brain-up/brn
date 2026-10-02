package com.epam.brn.integration

import com.epam.brn.enums.BrnGender
import com.epam.brn.enums.BrnRole
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Role
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.SubGroup
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.RoleRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.UserAccountService
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.random.Random

@WithMockUser(username = "test@test.test", roles = [BrnRole.USER])
class StudyHistoryIT : BaseIT() {
    private val baseUrl = "/study-history"

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    lateinit var subGroupRepository: SubGroupRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    @Autowired
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var userAccountService: UserAccountService

    @AfterEach
    fun deleteAfterTest() {
        studyHistoryRepository.deleteAll()
        exerciseRepository.deleteAll()
        subGroupRepository.deleteAll()
        seriesRepository.deleteAll()
        exerciseGroupRepository.deleteAll()
        userAccountRepository.deleteAll()
        roleRepository.deleteAll()
    }

    @Test
    fun `test repo get last study histories for user`() {
        // GIVEN
        val existingUser = insertUser()
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val existingExerciseSecond = insertExercise(exerciseSecondName, subGroup)
        val now = LocalDateTime.now()
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now.minusHours(1))
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.minusHours(1))
        val historySecondExerciseTwo = insertStudyHistory(existingUser, existingExerciseSecond, now)
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
        val result = existingUser.id?.let { studyHistoryRepository.findLastByUserAccountId(it) }
        // THEN
        result?.size shouldBe 2
    }

    @Test
    fun `test repo get last study histories for user and exercises`() {
        // GIVEN
        val existingUser = insertUser()
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val existingExerciseSecond = insertExercise(exerciseSecondName, subGroup)
        val now = LocalDateTime.now()
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now.minusHours(1))
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.minusHours(1))
        val historySecondExerciseTwo = insertStudyHistory(existingUser, existingExerciseSecond, now)
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
        val result =
            existingUser.id?.let {
                studyHistoryRepository.findLastByUserAccountIdAndExercises(
                    it,
                    listOf(existingExerciseFirst.id!!),
                )
            }
        // THEN
        result?.size shouldBe 1
    }

    @Test
    fun `test repo day timer for user`() {
        // GIVEN
        val existingUser = insertUser()
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val existingExerciseSecond = insertExercise(exerciseSecondName, subGroup)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now.plusHours(1))
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.plusHours(1))
        val historySecondExerciseTwo = insertStudyHistory(existingUser, existingExerciseSecond, now)
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
        val result =
            existingUser.id?.let {
                studyHistoryRepository
                    .getDayTimer(it, Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))
            }
        // THEN
        result shouldBe 488
    }

    @Test
    fun `test get day timer for current user`() {
        // GIVEN
        val existingUser = insertUser()
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val existingExerciseSecond = insertExercise(exerciseSecondName, subGroup)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now.plusHours(1))
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.plusHours(1))
        val historySecondExerciseTwo = insertStudyHistory(existingUser, existingExerciseSecond, now)
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
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .get("$baseUrl/todayTimer")
                    .contentType(MediaType.APPLICATION_JSON),
            )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data").value(488))
    }

    @Test
    fun `test get today timer for current user without study history records`() {
        // GIVEN
        insertUser()
        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .get("$baseUrl/todayTimer")
                    .contentType(MediaType.APPLICATION_JSON),
            )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data").value(0))
    }

    @Test
    fun `test repo get today timer for current user without study history records`() {
        // GIVEN
        val user = insertUser()
        // WHEN
        val result = studyHistoryRepository.getTodayDayTimer(user.id!!)
        // THEN
        result shouldBe 0
    }

    @Test
    fun `test delete study history when delete autotest users`() {
        // GIVEN
        val roleUser = insertRole(BrnRole.USER)

        val user1 =
            UserAccount(
                fullName = "autotest_n1",
                email = "autotest_n@1704819771.8820736.com",
                gender = BrnGender.MALE.toString(),
                bornYear = 2000,
                active = true,
            )
        user1.roleSet = mutableSetOf(roleUser)

        val user2 =
            UserAccount(
                fullName = "autotest_n1",
                email = "autotest_n@170472339.1784415.com",
                gender = BrnGender.MALE.toString(),
                bornYear = 2000,
                active = true,
            )
        user2.roleSet = mutableSetOf(roleUser)

        userAccountRepository.saveAll(listOf(user1, user2))

        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise("FirstName", subGroup)
        val existingExerciseSecond = insertExercise("SecondName", subGroup)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val historyFirstExerciseOne = insertStudyHistory(user1, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(user2, existingExerciseSecond, now)

        studyHistoryRepository
            .saveAll(
                listOf(
                    historyFirstExerciseOne,
                    historySecondExerciseOne,
                ),
            )

        // WHEN
        val count = userAccountService.deleteAutoTestUsers()

        val result1 =
            user1.id?.let {
                studyHistoryRepository.findLastByUserAccountIdAndExercises(
                    it,
                    listOf(existingExerciseFirst.id!!),
                )
            }

        val result2 =
            user1.id?.let {
                studyHistoryRepository.findLastByUserAccountIdAndExercises(
                    it,
                    listOf(existingExerciseFirst.id!!),
                )
            }

        // THEN
        count shouldBe 2L
        result1?.size shouldBe 0
        result2?.size shouldBe 0
    }

    @Test
    fun `test delete study history when delete single autotest user`() {
        // GIVEN
        val roleUser = insertRole(BrnRole.USER)
        val email = "autotest_n@1704819771.8820736.com"

        val user1 =
            UserAccount(
                fullName = "autotest_n1",
                email = email,
                gender = BrnGender.MALE.toString(),
                bornYear = 2000,
                active = true,
            )
        user1.roleSet = mutableSetOf(roleUser)
        userAccountRepository.save(user1)

        val exerciseFirstName = "FirstName"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val historyFirstExerciseTwo = insertStudyHistory(user1, existingExerciseFirst, now)

        studyHistoryRepository.save(historyFirstExerciseTwo)

        // WHEN
        val count = userAccountService.deleteAutoTestUserByEmail(email)

        val result1 =
            user1.id?.let {
                studyHistoryRepository.findLastByUserAccountIdAndExercises(
                    it,
                    listOf(existingExerciseFirst.id!!),
                )
            }

        // THEN
        count shouldBe 1L
        result1?.size shouldBe 0
    }

    @Test
    fun `getStatisticsByUserAccountIds should return all-time stats per user in one query`() {
        // GIVEN
        val user1 = insertUser("batch-user1@test.test")
        val user2 = insertUser("batch-user2@test.test")
        val series = insertSeries()
        val subGroup = insertSubGroup(series)
        val exerciseA = insertExercise("A", subGroup)
        val exerciseB = insertExercise("B", subGroup)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        studyHistoryRepository.saveAll(
            listOf(
                insertStudyHistory(user1, exerciseA, now.minusDays(1)),
                insertStudyHistory(user1, exerciseB, now),
                insertStudyHistory(user2, exerciseA, now),
            ),
        )

        // WHEN
        val stats = studyHistoryRepository.getStatisticsByUserAccountIds(listOf(user1.id!!, user2.id!!))

        // THEN
        stats.size shouldBe 2
        val user1Stats = stats.first { it.userId == user1.id }
        user1Stats.doneExercises shouldBe 2
        user1Stats.firstStudy shouldBe now.minusDays(1)
        user1Stats.lastStudy shouldBe now
        val user2Stats = stats.first { it.userId == user2.id }
        user2Stats.doneExercises shouldBe 1
    }

    @Test
    fun `getHistoriesForUsers should return histories for the given users within the period`() {
        // GIVEN
        val user1 = insertUser("batch-user1@test.test")
        val user2 = insertUser("batch-user2@test.test")
        val series = insertSeries()
        val subGroup = insertSubGroup(series)
        val exerciseA = insertExercise("A", subGroup)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        studyHistoryRepository.saveAll(
            listOf(
                insertStudyHistory(user1, exerciseA, now.minusDays(1)),
                insertStudyHistory(user1, exerciseA, now),
                insertStudyHistory(user2, exerciseA, now),
                insertStudyHistory(user1, exerciseA, now.minusDays(10)), // out of range
            ),
        )

        // WHEN
        val histories =
            studyHistoryRepository.getHistoriesForUsers(
                listOf(user1.id!!, user2.id!!),
                now.minusDays(2),
                now.plusDays(1),
            )

        // THEN
        histories.size shouldBe 3
        histories.map { it.userAccount.id } shouldBe listOf(user1.id, user1.id, user2.id)
    }

    @Test
    fun `countStudyDaysForUsers should count distinct exercising days per user`() {
        // GIVEN
        val user1 = insertUser("batch-user1@test.test")
        val user2 = insertUser("batch-user2@test.test")
        val series = insertSeries()
        val subGroup = insertSubGroup(series)
        val exerciseA = insertExercise("A", subGroup)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        studyHistoryRepository.saveAll(
            listOf(
                insertStudyHistory(user1, exerciseA, now.plusHours(1)),
                insertStudyHistory(user1, exerciseA, now.plusHours(3)), // same day as above
                insertStudyHistory(user1, exerciseA, now.minusDays(1)),
                insertStudyHistory(user2, exerciseA, now),
            ),
        )

        // WHEN
        val studyDays =
            studyHistoryRepository.countStudyDaysForUsers(
                listOf(user1.id!!, user2.id!!),
                now.minusDays(5),
                now.plusDays(1),
            )

        // THEN
        studyDays.first { it.userId == user1.id }.studyDays shouldBe 2
        studyDays.first { it.userId == user2.id }.studyDays shouldBe 1
    }

    private fun insertStudyHistory(
        existingUser: UserAccount,
        existingExercise: Exercise,
        startTime: LocalDateTime,
    ): StudyHistory = studyHistoryRepository.save(
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

    private fun insertUser(): UserAccount = insertUser("test@test.test")

    private fun insertUser(email: String): UserAccount = userAccountRepository.save(
        UserAccount(
            fullName = "testUserFirstName",
            gender = BrnGender.MALE.toString(),
            bornYear = 2000,
            email = email,
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
                type = "type",
            ),
        )
    }

    private fun insertSubGroup(series: Series): SubGroup = subGroupRepository.save(
        SubGroup(series = series, level = 1, code = "code", name = "subGroup name"),
    )

    fun insertExercise(
        exerciseName: String,
        subGroup: SubGroup,
    ): Exercise = exerciseRepository.save(
        Exercise(
            subGroup = subGroup,
            level = 0,
            name = exerciseName,
        ),
    )

    private fun insertRole(roleName: String): Role = roleRepository.save(Role(name = roleName))
}
