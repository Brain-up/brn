package com.epam.brn.service

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.AudiometryHistoryRequest
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.AudiometryHistory
import com.epam.brn.model.AudiometryTask
import com.epam.brn.model.Gender
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AudiometryHistoryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime.now
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
        val headphonesDto = HeadphonesDto(name = "test", type = HeadphonesType.IN_EAR_BLUETOOTH, id = 5L)
        val headphonesEntity = headphonesDto.toEntity()
        val userAccount = UserAccount(
            id = 1L,
            fullName = "testUserFirstName",
            gender = Gender.MALE.toString(),
            bornYear = 2000,
            password = "test",
            email = "test@gmail.com",
            active = true,
            headphones = mutableSetOf(headphonesEntity)
        )
        `when`(userAccountService.getCurrentUser()).thenReturn(userAccount)
        val audiometryHistoryRequest = mock(AudiometryHistoryRequest::class.java)
        val audiometryTask = mock(AudiometryTask::class.java)
        val audiometryHistory = spy(
            AudiometryHistory(
                id = 2L,
                userAccount = userAccount,
                audiometryTask = audiometryTask,
                startTime = now(),
                tasksCount = 4,
                rightAnswers = 1,
                executionSeconds = 1,
                headphones = headphonesEntity
            )
        )
        `when`(audiometryHistoryRequest.audiometryTaskId).thenReturn(1L)
        `when`(audiometryTaskRepository.findById(1L)).thenReturn(Optional.of(audiometryTask))
        `when`(audiometryHistoryRequest.toEntity(userAccount, audiometryTask, headphonesEntity)).thenReturn(
            audiometryHistory
        )
        `when`(audiometryHistoryRepository.save(audiometryHistory)).thenReturn(audiometryHistory)
        `when`(audiometryHistory.id).thenReturn(2L)
        `when`(audiometryHistoryRequest.headphones).thenReturn(5L)

        // WHEN
        val result = audiometryHistoryService.save(audiometryHistoryRequest)
        // THEN
        assertEquals(2L, result)
        verify(userAccountService).getCurrentUser()
        verify(audiometryTaskRepository).findById(1L)
        verify(audiometryHistoryRepository).save(argThat { headphones != null })
        verify(audiometryHistoryRepository).save(audiometryHistory)
    }

    @Test
    fun `should throw an exception when there is no headphones by user`() {
        // GIVEN
        val headphonesDto = HeadphonesDto(name = "test", type = HeadphonesType.IN_EAR_BLUETOOTH, id = 5L)
        val headphonesEntity = headphonesDto.toEntity()
        val userAccount = UserAccount(
            id = 1L,
            fullName = "testUserFirstName",
            gender = Gender.MALE.toString(),
            bornYear = 2000,
            password = "test",
            email = "test@gmail.com",
            active = true,
            headphones = mutableSetOf(headphonesEntity)
        )
        val audiometryHistoryRequest = mock(AudiometryHistoryRequest::class.java)
        val audiometryTask = mock(AudiometryTask::class.java)

        // WHEN
        `when`(userAccountService.getCurrentUser()).thenReturn(userAccount)
        `when`(audiometryHistoryRequest.audiometryTaskId).thenReturn(1L)
        `when`(audiometryTaskRepository.findById(1L)).thenReturn(Optional.of(audiometryTask))

        assertFailsWith<IllegalArgumentException> {
            audiometryHistoryService.save(audiometryHistoryRequest)
        }
    }

    @Test
    fun `should throw an exception when audiometry task is not found`() {
        // GIVEN
        val userAccount = UserAccount(
            id = 1L,
            fullName = "testUserFirstName",
            gender = Gender.MALE.toString(),
            bornYear = 2000,
            password = "test",
            email = "test@gmail.com",
            active = true,
            headphones = mutableSetOf()
        )
        val audiometryHistoryRequest = mock(AudiometryHistoryRequest::class.java)

        // WHEN
        `when`(userAccountService.getCurrentUser()).thenReturn(userAccount)
        `when`(audiometryHistoryRequest.audiometryTaskId).thenReturn(1L)
        `when`(audiometryTaskRepository.findById(1L)).thenReturn(Optional.empty())

        assertFailsWith<EntityNotFoundException> {
            audiometryHistoryService.save(audiometryHistoryRequest)
        }
    }
}
