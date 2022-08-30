package com.epam.brn.controller

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.ResourceDto
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.request.SubGroupChangeRequest
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.dto.request.exercise.ExercisePhrasesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseSentencesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseWordsCreateDto
import com.epam.brn.dto.request.exercise.Phrases
import com.epam.brn.dto.request.exercise.SetOfWords
import com.epam.brn.dto.response.ExerciseWithTasksResponse
import com.epam.brn.dto.response.SubGroupResponse
import com.epam.brn.enums.Locale
import com.epam.brn.service.ExerciseService
import com.epam.brn.service.ResourceService
import com.epam.brn.service.StudyHistoryService
import com.epam.brn.service.SubGroupService
import com.epam.brn.upload.CsvUploadService
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.multipart.MultipartFile

@ExtendWith(MockKExtension::class)
internal class AdminExerciseFlowControllerTest {

    @InjectMockKs
    private lateinit var adminExerciseFlowController: AdminExerciseFlowController

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
    private lateinit var file: MultipartFile

    @MockK
    private lateinit var request: UpdateResourceDescriptionRequest

    @MockK
    private lateinit var studyHistoryDto: StudyHistoryDto

    @MockK
    private lateinit var exerciseWithTasksResponse: ExerciseWithTasksResponse

    @MockK
    private lateinit var resourceDto: ResourceDto

    @Test
    fun `getMonthHistories should return month histories`() {
        // GIVEN
        val userId = 1L
        val month = 1
        val year = 2021
        every { studyHistoryService.getMonthHistories(userId, month, year) } returns listOf(studyHistoryDto)

        // WHEN
        val monthHistories = adminExerciseFlowController.getMonthHistories(userId, month, year)

        // THEN
        verify(exactly = 1) { studyHistoryService.getMonthHistories(userId, month, year) }
        monthHistories.statusCodeValue shouldBe HttpStatus.SC_OK
        monthHistories.body!!.data shouldBe listOf(studyHistoryDto)
    }

    @Test
    fun `loadExercises should return http status 201`() {
        // GIVEN
        val seriesId = 1L
        every { csvUploadService.loadExercises(seriesId, file) } just Runs

        // WHEN
        val loadExercises = adminExerciseFlowController.loadExercises(seriesId, file)

        // THEN
        loadExercises.statusCodeValue shouldBe HttpStatus.SC_CREATED
    }

    @Test
    fun `getExercisesBySubGroup should return data with http status 200`() {
        // GIVEN
        val subGroupId = 1L
        every { exerciseService.findExercisesWithTasksBySubGroup(subGroupId) } returns listOf(exerciseWithTasksResponse)

        // WHEN
        val exercises = adminExerciseFlowController.getExercisesBySubGroup(subGroupId)

        // THEN
        verify(exactly = 1) { exerciseService.findExercisesWithTasksBySubGroup(subGroupId) }
        exercises.statusCodeValue shouldBe HttpStatus.SC_OK
        exercises.body!!.data shouldBe listOf(exerciseWithTasksResponse)
    }

    @Test
    fun updateResourceDescription() {
        // GIVEN
        val id = 1L
        val description = "description"
        every { request.description } returns description
        every { resourceService.updateDescription(id, description) } returns resourceDto

        // WHEN
        val updated = adminExerciseFlowController.updateResourceDescription(id, request)

        // THEN
        verify(exactly = 1) { resourceService.updateDescription(id, description) }
        updated.statusCodeValue shouldBe HttpStatus.SC_OK
        updated.body!!.data shouldBe resourceDto
    }

    @Test
    fun `addSubGroupToSeries should return http status 204`() {
        // GIVEN
        val seriesId = 1L
        val subGroupRequest = SubGroupRequest("Test name", 1, "shortWords", "Test description")
        val subGroupResponse = mockkClass(SubGroupResponse::class, relaxed = true)

        every { subGroupService.addSubGroupToSeries(subGroupRequest, seriesId) } returns subGroupResponse

        // WHEN
        val createdSubGroup = adminExerciseFlowController.addSubGroupToSeries(seriesId, subGroupRequest)

        // THEN
        createdSubGroup.statusCodeValue shouldBe HttpStatus.SC_CREATED
    }

    @Test
    fun `createExerciseWords should return http status 204`() {
        // GIVEN
        val exerciseWordsCreateDto = ExerciseWordsCreateDto(
            locale = Locale.RU,
            subGroup = "subGroup",
            level = 1,
            exerciseName = "exerciseName",
            words = listOf("word1", "word2"),
            noiseLevel = 0
        )
        val exerciseDto = mockk<ExerciseDto>()
        every { exerciseService.createExercise(exerciseWordsCreateDto) } returns exerciseDto

        // WHEN
        val createdExercise = adminExerciseFlowController.createExercise(exerciseWordsCreateDto)

        // THEN
        verify(exactly = 1) { exerciseService.createExercise(exerciseWordsCreateDto) }
        createdExercise.statusCodeValue shouldBe HttpStatus.SC_CREATED
    }

    @Test
    fun `createExercisePhrases should return http status 204`() {
        // GIVEN
        val exercisePhrasesCreateDto = ExercisePhrasesCreateDto(
            locale = Locale.RU,
            subGroup = "subGroup",
            level = 1,
            exerciseName = "exerciseName",
            phrases = Phrases(shortPhrase = "shortPhrase", longPhrase = "longPhrase"),
            noiseLevel = 0
        )
        val exerciseDto = mockk<ExerciseDto>()
        every { exerciseService.createExercise(exercisePhrasesCreateDto) } returns exerciseDto

        // WHEN
        val createdExercise = adminExerciseFlowController.createExercise(exercisePhrasesCreateDto)

        // THEN
        verify(exactly = 1) { exerciseService.createExercise(exercisePhrasesCreateDto) }
        createdExercise.statusCodeValue shouldBe HttpStatus.SC_CREATED
    }

    @Test
    fun `createExerciseSentences should return http status 204`() {
        // GIVEN
        val exerciseSentencesCreateDto = ExerciseSentencesCreateDto(
            locale = Locale.RU,
            subGroup = "subGroup",
            level = 1,
            exerciseName = "exerciseName",
            orderNumber = 1,
            words = SetOfWords()
        )
        val exerciseDto = mockk<ExerciseDto>()
        every { exerciseService.createExercise(exerciseSentencesCreateDto) } returns exerciseDto

        // WHEN
        val createdExercise = adminExerciseFlowController.createExercise(exerciseSentencesCreateDto)

        // THEN
        verify(exactly = 1) { exerciseService.createExercise(exerciseSentencesCreateDto) }
        createdExercise.statusCodeValue shouldBe HttpStatus.SC_CREATED
    }

    @Test
    fun `updateSubGroupById should update subGroup by subGroupId`() {
        // GIVEN
        val subGroupId = 1L
        val subGroupChangeRequest = SubGroupChangeRequest(withPictures = true)
        val updatedSubGroup = mockkClass(SubGroupResponse::class, relaxed = true)
        every { subGroupService.updateSubGroupById(subGroupId, subGroupChangeRequest) } returns updatedSubGroup

        // WHEN
        val actual = adminExerciseFlowController.updateSubGroupById(subGroupId, subGroupChangeRequest)

        // THEN
        actual.statusCode.value() shouldBe HttpStatus.SC_OK
        actual.body!!.data shouldBe updatedSubGroup
    }
}
