package com.epam.brn.service

import com.epam.brn.dto.request.AudiometryHistoryRequest
import com.epam.brn.model.AudiometryHistory
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.Gender
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AudiometryHistoryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class AudiometryHistoryServiceTest {
    @InjectMocks
    lateinit var audiometryHistoryService: AudiometryHistoryService

    @Mock
    lateinit var audiometryTaskRepository: AudiometryTaskRepository

    @Mock
    lateinit var audiometryHistoryRepository: AudiometryHistoryRepository

    @Mock
    lateinit var userAccountService: UserAccountService

    @Test
    fun `should create audiometryHistory`() {
        // GIVEN
        val userAccount = UserAccount(
            id = 1L,
            fullName = "testUserFirstName",
            gender = Gender.MALE.toString(),
            bornYear = 2000,
            password = "test",
            email = "test@gmail.com",
            active = true
        )
        `when`(userAccountService.getCurrentUser()).thenReturn(userAccount)
        val audiometryHistoryRequest = mock(AudiometryHistoryRequest::class.java)
        val audiometryTask = mock(AudiometryTask::class.java)
        val audiometryHistory = mock(AudiometryHistory::class.java)
        `when`(audiometryHistoryRequest.audiometryTaskId).thenReturn(1L)
        `when`(audiometryTaskRepository.findById(1L)).thenReturn(Optional.of(audiometryTask))
        `when`(audiometryHistoryRequest.toEntity(userAccount, audiometryTask)).thenReturn(audiometryHistory)
        `when`(audiometryHistoryRepository.save(audiometryHistory)).thenReturn(audiometryHistory)
        `when`(audiometryHistory.id).thenReturn(2L)

        // WHEN
        val result = audiometryHistoryService.save(audiometryHistoryRequest)
        // THEN
        assertEquals(2L, result)
        verify(userAccountService).getCurrentUser()
        verify(audiometryTaskRepository).findById(1L)
        verify(audiometryHistoryRepository).save(audiometryHistory)
    }
}
