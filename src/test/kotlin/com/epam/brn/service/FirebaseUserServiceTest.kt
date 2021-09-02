package com.epam.brn.service

import com.google.firebase.auth.FirebaseAuth
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class FirebaseUserServiceTest {

    @InjectMockKs
    lateinit var firebaseUserService: FirebaseUserService

    @MockK
    lateinit var firebaseAuth: FirebaseAuth

/*    @Test
    fun `should get user by uid`() {
        // GIVEN
        val userMock: UserRecord = mockkClass(UserRecord::class)
        val userDtoMock = (2, 1, "name", 1, NoiseDto(0, noiseUrl))
        val exerciseId = 1L
        every { exerciseMock.toDto(true) } returns exerciseDtoMock
        every { exerciseMock.id } returns exerciseId
        every { studyHistoryRepository.getDoneExercisesIdList(ofType(Long::class)) } returns listOf(exerciseId)
        every { exerciseRepository.findAll() } returns listOf(exerciseMock)
        every { urlConversionService.makeUrlForNoise(noiseUrl) } returns noiseUrl

        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserId(22L)

        // THEN
        assertEquals(actualResult, listOf(exerciseDtoMock))
        verify(exactly = 1) { exerciseRepository.findAll() }
        verify(exactly = 1) { studyHistoryRepository.getDoneExercisesIdList(ofType(Long::class)) }
    }*/
}
