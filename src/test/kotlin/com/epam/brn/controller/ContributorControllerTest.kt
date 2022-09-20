package com.epam.brn.controller

import com.epam.brn.dto.request.contributor.ContributorRequest
import com.epam.brn.dto.response.ContributorResponse
import com.epam.brn.enums.ContributorType
import com.epam.brn.service.ContributorService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class ContributorControllerTest {

    @InjectMockKs
    lateinit var contributorController: ContributorController

    @MockK
    lateinit var contributorService: ContributorService

    @Test
    fun `should get all contributors`() {
        // GIVEN
        val contributor = mockk<ContributorResponse>()
        val contributorList = listOf(contributor)
        val locale = "ru-ru"
        every { contributorService.getContributors(locale, ContributorType.SPECIALIST) } returns contributorList

        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: List<ContributorResponse> =
            contributorController.getContributors(
                locale,
                ContributorType.SPECIALIST
            ).body?.data as List<ContributorResponse>

        // THEN
        verify(exactly = 1) { contributorService.getContributors(locale, ContributorType.SPECIALIST) }
        assertTrue(actualResultData.contains(contributor))
    }

    @Test
    fun `should add contributor`() {
        // GIVEN
        val contributorDto = mockk<ContributorRequest>()
        val contributorResponse = mockk<ContributorResponse>()
        every { contributorService.createContributor(contributorDto) } returns contributorResponse

        // WHEN
        val actualResultData = contributorController.createContributor(contributorDto).body?.data as ContributorResponse

        // THEN
        verify(exactly = 1) { contributorService.createContributor(contributorDto) }
        assertEquals(actualResultData, contributorResponse)
    }

    @Test
    fun `should update contributor`() {
        val contributorId = 1L
        val contributorDto = mockk<ContributorRequest>()
        val contributorResponse = mockk<ContributorResponse>()
        every { contributorService.updateContributor(contributorId, contributorDto) } returns contributorResponse

        // WHEN
        val actualResultData =
            contributorController.updateContributor(contributorId, contributorDto).body?.data as ContributorResponse

        // THEN
        verify(exactly = 1) { contributorService.updateContributor(contributorId, contributorDto) }
        assertEquals(actualResultData, contributorResponse)
    }
}
