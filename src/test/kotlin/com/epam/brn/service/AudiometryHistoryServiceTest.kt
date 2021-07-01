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
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime.now
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
internal class AudiometryHistoryServiceTest {
    @InjectMockKs
    lateinit var audiometryHistoryService: AudiometryHistoryService

    @MockK
    lateinit var audiometryTaskRepository: AudiometryTaskRepository

    @MockK
    lateinit var audiometryHistoryRepository: AudiometryHistoryRepository

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    lateinit var audiometryHistoryRequest: AudiometryHistoryRequest

    @MockK
    lateinit var audiometryTask: AudiometryTask

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
        every { userAccountService.getCurrentUser() } returns userAccount
        val audiometryHistory = spyk(
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
        every { audiometryHistoryRequest.audiometryTaskId } returns 1L
        every { audiometryTaskRepository.findById(1L) } returns Optional.of(audiometryTask)
        every {
            audiometryHistoryRequest.toEntity(
                userAccount,
                audiometryTask,
                headphonesEntity
            )
        } returns audiometryHistory
        every { audiometryHistoryRepository.save(audiometryHistory) } returns audiometryHistory
        every { audiometryHistory.id } returns 2L
        every { audiometryHistoryRequest.headphones } returns 5L

        // WHEN
        val result = audiometryHistoryService.save(audiometryHistoryRequest)
        // THEN
        assertEquals(2L, result)
        verify { userAccountService.getCurrentUser() }
        verify { audiometryTaskRepository.findById(1L) }
        verify { audiometryHistoryRepository.save(audiometryHistory) }
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
        every { userAccountService.getCurrentUser() } returns userAccount
        every { audiometryHistoryRequest.audiometryTaskId } returns 1L
        every { audiometryTaskRepository.findById(1L) } returns Optional.of(audiometryTask)
        every { audiometryHistoryRequest.headphones } returns null

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
        every { userAccountService.getCurrentUser() } returns userAccount
        every { audiometryHistoryRequest.audiometryTaskId } returns 1L
        every { audiometryTaskRepository.findById(1L) } returns Optional.empty()

        assertFailsWith<EntityNotFoundException> {
            audiometryHistoryService.save(audiometryHistoryRequest)
        }
    }
}
