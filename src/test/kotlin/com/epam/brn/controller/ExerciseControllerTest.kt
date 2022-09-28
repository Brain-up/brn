package com.epam.brn.controller

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.NoiseDto
import com.epam.brn.dto.request.exercise.ExercisePhrasesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseSentencesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseWordsCreateDto
import com.epam.brn.dto.request.exercise.Phrases
import com.epam.brn.dto.request.exercise.SetOfWords
import com.epam.brn.enums.AuthorityType
import com.epam.brn.enums.BrnLocale
import com.epam.brn.service.ExerciseService
import com.epam.brn.upload.CsvUploadService
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.multipart.MultipartFile

@ExtendWith(MockKExtension::class)
internal class ExerciseControllerTest {

    @InjectMockKs
    lateinit var exerciseController: ExerciseController

    @MockK
    lateinit var exerciseService: ExerciseService

    @MockK
    lateinit var csvUploadService: CsvUploadService

    @MockK
    lateinit var authorityService: AuthorityService

    @Test
    fun `should get exercises for user and series`() {
        // GIVEN
        val subGroupId: Long = 2
        val exercise = ExerciseDto(subGroupId, 1, "name", 1, NoiseDto(0, ""))
        val listExercises = listOf(exercise)
        every { exerciseService.findExercisesBySubGroupForCurrentUser(subGroupId) } returns listExercises
        every { authorityService.isCurrentUserHasAuthority(ofType(AuthorityType::class)) } returns false

        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: List<ExerciseDto> =
            exerciseController.getExercisesBySubGroup(subGroupId).body?.data as List<ExerciseDto>

        // THEN
        verify(exactly = 1) { exerciseService.findExercisesBySubGroupForCurrentUser(subGroupId) }
        assertTrue(actualResultData.contains(exercise))
    }

    @Test
    fun `should get exercise by id`() {
        // GIVEN
        val exerciseID: Long = 1
        val exercise = ExerciseDto(2, 1, "exe", 1, NoiseDto(0, ""))
        every { exerciseService.findExerciseById(exerciseID) } returns exercise

        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: ExerciseDto = exerciseController.getExercisesByID(exerciseID).body?.data as ExerciseDto

        // THEN
        verify(exactly = 1) { exerciseService.findExerciseById(exerciseID) }
        assertEquals(actualResultData, exercise)
    }

    @Test
    fun `loadExercises should return http status 201`() {
        // GIVEN
        val seriesId = 1L
        val file = mockk<MultipartFile>()
        every { csvUploadService.loadExercises(seriesId, file) } just Runs

        // WHEN
        val loadExercises = exerciseController.loadExercises(seriesId, file)

        // THEN
        loadExercises.statusCodeValue shouldBe HttpStatus.SC_CREATED
    }

    @Test
    fun `createExerciseWords should return http status 204`() {
        // GIVEN
        val exerciseWordsCreateDto = ExerciseWordsCreateDto(
            locale = BrnLocale.RU,
            subGroup = "subGroup",
            level = 1,
            exerciseName = "exerciseName",
            words = listOf("word1", "word2"),
            noiseLevel = 0
        )
        val exerciseDto = mockk<ExerciseDto>()
        every { exerciseService.createExercise(exerciseWordsCreateDto) } returns exerciseDto

        // WHEN
        val createdExercise = exerciseController.createExercise(exerciseWordsCreateDto)

        // THEN
        verify(exactly = 1) { exerciseService.createExercise(exerciseWordsCreateDto) }
        createdExercise.statusCodeValue shouldBe HttpStatus.SC_CREATED
    }

    @Test
    fun `createExercisePhrases should return http status 204`() {
        // GIVEN
        val exercisePhrasesCreateDto = ExercisePhrasesCreateDto(
            locale = BrnLocale.RU,
            subGroup = "subGroup",
            level = 1,
            exerciseName = "exerciseName",
            phrases = Phrases(shortPhrase = "shortPhrase", longPhrase = "longPhrase"),
            noiseLevel = 0
        )
        val exerciseDto = mockk<ExerciseDto>()
        every { exerciseService.createExercise(exercisePhrasesCreateDto) } returns exerciseDto

        // WHEN
        val createdExercise = exerciseController.createExercise(exercisePhrasesCreateDto)

        // THEN
        verify(exactly = 1) { exerciseService.createExercise(exercisePhrasesCreateDto) }
        createdExercise.statusCodeValue shouldBe HttpStatus.SC_CREATED
    }

    @Test
    fun `createExerciseSentences should return http status 204`() {
        // GIVEN
        val exerciseSentencesCreateDto = ExerciseSentencesCreateDto(
            locale = BrnLocale.RU,
            subGroup = "subGroup",
            level = 1,
            exerciseName = "exerciseName",
            orderNumber = 1,
            words = SetOfWords()
        )
        val exerciseDto = mockk<ExerciseDto>()
        every { exerciseService.createExercise(exerciseSentencesCreateDto) } returns exerciseDto

        // WHEN
        val createdExercise = exerciseController.createExercise(exerciseSentencesCreateDto)

        // THEN
        verify(exactly = 1) { exerciseService.createExercise(exerciseSentencesCreateDto) }
        createdExercise.statusCodeValue shouldBe HttpStatus.SC_CREATED
    }

    @Test
    fun `getExercisesBySubGroup should return data with http status 200`() {
        // GIVEN
        val subGroupId = 1L
        val exerciseResponse = mockk<ExerciseDto>()
        every { exerciseService.findExercisesWithTasksBySubGroup(subGroupId) } returns listOf(exerciseResponse)
        every { authorityService.isCurrentUserHasAuthority(ofType(AuthorityType::class)) } returns true

        // WHEN
        val exercises = exerciseController.getExercisesBySubGroup(subGroupId)

        // THEN
        verify(exactly = 1) { exerciseService.findExercisesWithTasksBySubGroup(subGroupId) }
        exercises.statusCodeValue shouldBe HttpStatus.SC_OK
        exercises.body!!.data shouldBe listOf(exerciseResponse)
    }
}
