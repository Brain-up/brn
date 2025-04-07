package com.epam.brn.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class FirebaseUserServiceTest {
    @InjectMockKs
    lateinit var firebaseUserService: FirebaseUserService

    @MockK
    lateinit var firebaseAuth: FirebaseAuth

    @Test
    fun `should get user by uid`() {
        // GIVEN
        val uuid = "123456789"
        val userMock = mockk<UserRecord>()
        every { firebaseAuth.getUser(uuid) } returns userMock

        // WHEN
        val result = firebaseUserService.getUserByUuid(uuid)

        // THEN
        assertEquals(userMock, result)
        verify(exactly = 1) { firebaseAuth.getUser(uuid) }
        verify(exactly = 0) { firebaseAuth.getUserByEmail(any()) }
    }
}
