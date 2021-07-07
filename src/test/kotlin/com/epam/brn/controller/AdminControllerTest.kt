package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.ExerciseWithTasksDto
import com.epam.brn.dto.ResourceDto
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.SubGroupDto
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.dto.response.UserWithAnalyticsDto
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.SubGroupService
import com.epam.brn.service.statistic.UserPeriodStatisticService
import com.epam.brn.upload.CsvUploadService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import io.mockk.mockkClass
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Pageable
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class AdminControllerTest {

    @InjectMockKs
    private lateinit var adminController: AdminController

    @MockK
    private lateinit var userAccountService: UserAccountService

    @MockK
    private lateinit var userDayStatisticService: UserPeriodStatisticService<DayStudyStatistic>

    @MockK
    private lateinit var userMonthStatisticService: UserPeriodStatisticService<MonthStudyStatistic>

    @MockK
    private lateinit var studyHistoryService: StudyHistoryService

    @MockK
    private lateinit var exerciseService: ExerciseService

    @MockK
    private lateinit var csvUploadService: CsvUploadService

    @MockK
    private lateinit var resourceService: ResourceService

    @MockK
    private lateinit var subGroupService: SubGroupService

    @MockK
    private lateinit var pageable: Pageable

    @MockK
    private lateinit var file: MultipartFile

    @MockK
    private lateinit var request: UpdateResourceDescriptionRequest

    @MockK
    private lateinit var userWithAnalyticsDto: UserWithAnalyticsDto

    @MockK
    private lateinit var userAccountDto: UserAccountDto

    @MockK
    private lateinit var studyHistoryDto: StudyHistoryDto

    @MockK
    private lateinit var exerciseWithTasksDto: ExerciseWithTasksDto

    @MockK
    private lateinit var resourceDto: ResourceDto

    @MockK
    private lateinit var dayStudyStatistic: DayStudyStatistic

    @MockK
    private lateinit var monthStudyStatistic: MonthStudyStatistic

    @Test
    fun `getUsers should return users with statistic when withAnalytics is true`() {
        // GIVEN
        val withAnalytics = true
        every { userAccountService.getUsersWithAnalytics(pageable) } returns listOf(userWithAnalyticsDto)

        // WHEN
        val users = adminController.getUsers(withAnalytics, pageable)

        // THEN
        verify(exactly = 1) { userAccountService.getUsersWithAnalytics(pageable) }
        assertEquals(HttpStatus.SC_OK, users.statusCodeValue)
        assertEquals(listOf(userWithAnalyticsDto), (users.body as BaseResponseDto).data)
    }

    @Test
    fun `getUsers should return users when withAnalytics is false`() {
        // GIVEN
        val withAnalytics = false
        every { userAccountService.getUsers(pageable) } returns listOf(userAccountDto)

        // WHEN
        val users = adminController.getUsers(withAnalytics, pageable)

        // THEN
        verify(exactly = 1) { userAccountService.getUsers(pageable) }
        assertEquals(HttpStatus.SC_OK, users.statusCodeValue)
        assertEquals(listOf(userAccountDto), (users.body as BaseResponseDto).data)
    }

    @Test
    fun `getHistories should return histories`() {
        // GIVEN
        val userId = 1L
        val date = LocalDateTime.now()
        every { studyHistoryService.getHistories(userId, date, date) } returns listOf(studyHistoryDto)

        // WHEN
        val histories = adminController.getHistories(userId, date, date)

        // THEN
        verify(exactly = 1) { studyHistoryService.getHistories(userId, date, date) }
        assertEquals(HttpStatus.SC_OK, histories.statusCodeValue)
        assertEquals(listOf(studyHistoryDto), histories.body!!.data)
    }

    @Test
    fun `getMonthHistories should return month histories`() {
        // GIVEN
        val userId = 1L
        val month = 1
        val year = 2021
        every { studyHistoryService.getMonthHistories(userId, month, year) } returns listOf(studyHistoryDto)

        // WHEN
        val monthHistories = adminController.getMonthHistories(userId, month, year)

        // THEN
        verify(exactly = 1) { studyHistoryService.getMonthHistories(userId, month, year) }
        assertEquals(HttpStatus.SC_OK, monthHistories.statusCodeValue)
        assertEquals(listOf(studyHistoryDto), monthHistories.body!!.data)
    }

    @Test
    fun `loadExercises should return http status 201`() {
        // GIVEN
        val seriesId = 1L
        every { csvUploadService.loadExercises(seriesId, file) } just Runs

        // WHEN
        val loadExercises = adminController.loadExercises(seriesId, file)

        // THEN
        assertEquals(HttpStatus.SC_CREATED, loadExercises.statusCodeValue)
    }

    @Test
    fun `getExercisesBySubGroup should return data with http status 200`() {
        // GIVEN
        val subGroupId = 1L
        every { exerciseService.findExercisesWithTasksBySubGroup(subGroupId) } returns listOf(exerciseWithTasksDto)

        // WHEN
        val exercises = adminController.getExercisesBySubGroup(subGroupId)

        // THEN
        verify(exactly = 1) { exerciseService.findExercisesWithTasksBySubGroup(subGroupId) }
        assertEquals(HttpStatus.SC_OK, exercises.statusCodeValue)
        assertEquals(listOf(exerciseWithTasksDto), exercises.body!!.data)
    }

    @Test
    fun updateResourceDescription() {
        // GIVEN
        val id = 1L
        val description = "description"
        every { request.description } returns description
        every { resourceService.updateDescription(id, description) } returns resourceDto

        // WHEN
        val updated = adminController.updateResourceDescription(id, request)

        // THEN
        verify(exactly = 1) { resourceService.updateDescription(id, description) }
        assertEquals(HttpStatus.SC_OK, updated.statusCodeValue)
        assertEquals(resourceDto, updated.body!!.data)
    }

    @Test
    fun `getUserWeeklyStatistic should return weekly statistic`() {
        // GIVEN
        val userId = 1L
        val date = LocalDateTime.now()
        every { userDayStatisticService.getStatisticForPeriod(date, date, userId) } returns listOf(dayStudyStatistic)

        // WHEN
        val userWeeklyStatistic = adminController.getUserWeeklyStatistic(date, date, userId)

        // THEN
        verify(exactly = 1) { userDayStatisticService.getStatisticForPeriod(date, date, userId) }
        assertEquals(HttpStatus.SC_OK, userWeeklyStatistic.statusCodeValue)
        assertEquals(listOf(dayStudyStatistic), userWeeklyStatistic.body!!.data)
    }

    @Test
    fun `getUserYearlyStatistic should return yearly statistic`() {
        // GIVEN
        val userId = 1L
        val date = LocalDateTime.now()
        every {
            userMonthStatisticService.getStatisticForPeriod(
                date,
                date,
                userId
            )
        } returns listOf(monthStudyStatistic)

        // WHEN
        val userYearlyStatistic = adminController.getUserYearlyStatistic(date, date, userId)

        // THEN
        verify(exactly = 1) { userMonthStatisticService.getStatisticForPeriod(date, date, userId) }
        assertEquals(HttpStatus.SC_OK, userYearlyStatistic.statusCodeValue)
        assertEquals(listOf(monthStudyStatistic), userYearlyStatistic.body!!.data)
    }

    @Test
    fun `addSubGroupToSeries should return http status 204`() {
        // GIVEN
        val seriesId = 1L
        val subGroupRequest = SubGroupRequest(seriesId, 1, "Test name", "shortWords", "Test description")
        val subGroupDto = mockkClass(SubGroupDto::class, relaxed = true)

        every { subGroupService.addSubGroupToSeries(subGroupRequest, seriesId) } returns subGroupDto

        // WHEN
        val createdSubGroup = adminController.addSubGroupToSeries(seriesId, subGroupRequest)

        // THEN
        assertEquals(HttpStatus.SC_CREATED, createdSubGroup.statusCodeValue)
    }
}
