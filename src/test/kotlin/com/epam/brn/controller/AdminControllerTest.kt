package com.epam.brn.controller

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.ExerciseWithTasksDto
import com.epam.brn.dto.ResourceDto
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.dto.response.UserWithAnalyticsDto
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.UserAccountService
import com.epam.brn.upload.CsvUploadService
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Pageable
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class AdminControllerTest {

    @InjectMocks
    private lateinit var adminController: AdminController

    @Mock
    private lateinit var userAccountService: UserAccountService

    @Mock
    private lateinit var studyHistoryService: StudyHistoryService

    @Mock
    private lateinit var exerciseService: ExerciseService

    @Mock
    private lateinit var csvUploadService: CsvUploadService

    @Mock
    private lateinit var resourceService: ResourceService

    @Mock
    private lateinit var pageable: Pageable

    @Mock
    private lateinit var file: MultipartFile

    @Mock
    private lateinit var request: UpdateResourceDescriptionRequest

    @Mock
    private lateinit var userWithAnalyticsDto: UserWithAnalyticsDto

    @Mock
    private lateinit var userAccountDto: UserAccountDto

    @Mock
    private lateinit var studyHistoryDto: StudyHistoryDto

    @Mock
    private lateinit var exerciseWithTasksDto: ExerciseWithTasksDto

    @Mock
    private lateinit var resourceDto: ResourceDto

    @Test
    fun `getUsers should return users with statistic when withAnalytics is true`() {
        // GIVEN
        val withAnalytics = true
        `when`(userAccountService.getUsersWithAnalytics(pageable)).thenReturn(listOf(userWithAnalyticsDto))

        // WHEN
        val users = adminController.getUsers(withAnalytics, pageable)

        // THEN
        verify(userAccountService, times(1)).getUsersWithAnalytics(pageable)
        assertEquals(HttpStatus.SC_OK, users.statusCodeValue)
        assertEquals(listOf(userWithAnalyticsDto), (users.body as BaseResponseDto).data)
    }

    @Test
    fun `getUsers should return users when withAnalytics is false`() {
        // GIVEN
        val withAnalytics = false
        `when`(userAccountService.getUsers(pageable)).thenReturn(listOf(userAccountDto))

        // WHEN
        val users = adminController.getUsers(withAnalytics, pageable)

        // THEN
        verify(userAccountService, times(1)).getUsers(pageable)
        assertEquals(HttpStatus.SC_OK, users.statusCodeValue)
        assertEquals(listOf(userAccountDto), (users.body as BaseResponseDto).data)
    }

    @Test
    fun `getHistories should return histories`() {
        // GIVEN
        val userId = 1L
        val date = LocalDate.now()
        `when`(studyHistoryService.getHistories(userId, date, date)).thenReturn(listOf(studyHistoryDto))

        // WHEN
        val histories = adminController.getHistories(userId, date, date)

        // THEN
        verify(studyHistoryService, times(1)).getHistories(userId, date, date)
        assertEquals(HttpStatus.SC_OK, histories.statusCodeValue)
        assertEquals(listOf(studyHistoryDto), histories.body!!.data)
    }

    @Test
    fun `getMonthHistories should return month histories`() {
        // GIVEN
        val userId = 1L
        val month = 1
        val year = 2021
        `when`(studyHistoryService.getMonthHistories(userId, month, year)).thenReturn(listOf(studyHistoryDto))

        // WHEN
        val monthHistories = adminController.getMonthHistories(userId, month, year)

        // THEN
        verify(studyHistoryService, times(1)).getMonthHistories(userId, month, year)
        assertEquals(HttpStatus.SC_OK, monthHistories.statusCodeValue)
        assertEquals(listOf(studyHistoryDto), monthHistories.body!!.data)
    }

    @Test
    fun `loadExercises should return http status 201`() {
        // GIVEN
        val seriesId = 1L
        doNothing().`when`(csvUploadService).loadExercises(seriesId, file)

        // WHEN
        val loadExercises = adminController.loadExercises(seriesId, file)

        // THEN
        assertEquals(HttpStatus.SC_CREATED, loadExercises.statusCodeValue)
    }

    @Test
    fun `getExercisesBySubGroup should return data with http status 200`() {
        // GIVEN
        val subGroupId = 1L
        `when`(exerciseService.findExercisesWithTasksBySubGroup(subGroupId)).thenReturn(listOf(exerciseWithTasksDto))

        // WHEN
        val exercises = adminController.getExercisesBySubGroup(subGroupId)

        // THEN
        verify(exerciseService, times(1)).findExercisesWithTasksBySubGroup(subGroupId)
        assertEquals(HttpStatus.SC_OK, exercises.statusCodeValue)
        assertEquals(listOf(exerciseWithTasksDto), exercises.body!!.data)
    }

    @Test
    fun updateResourceDescription() {
        // GIVEN
        val id = 1L
        val description = "description"
        `when`(request.description).thenReturn(description)
        `when`(resourceService.updateDescription(id, description)).thenReturn(resourceDto)

        // WHEN
        val updated = adminController.updateResourceDescription(id, request)

        // THEN
        verify(resourceService, times(1)).updateDescription(id, description)
        assertEquals(HttpStatus.SC_OK, updated.statusCodeValue)
        assertEquals(resourceDto, updated.body!!.data)
    }
}
