package com.epam.brn.job

import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.impl.UserAccountServiceImpl
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils

@DisplayName("AutoTestUsersDeletionJob test using MockK")
@ExtendWith(MockKExtension::class)
class AutoTestUsersDeletionJobTest {
    @InjectMockKs
    lateinit var autoTestUsersDeletionJob: AutoTestUsersDeletionJob

    @MockK
    lateinit var userAccountService: UserAccountServiceImpl

    @MockK
    lateinit var userAccountRepository: UserAccountRepository

    @Test
    fun deleteAutoTestUsers() {
        // GIVEN
        val prefix = "autotest"
        ReflectionTestUtils.setField(userAccountService, "prefix", prefix)
        every { userAccountService.deleteAutoTestUsers() } returns 2L

        // WHEN
        autoTestUsersDeletionJob.deleteAutoTestUsers()

        // THEN
        verify { userAccountService.deleteAutoTestUsers() }
    }
}
