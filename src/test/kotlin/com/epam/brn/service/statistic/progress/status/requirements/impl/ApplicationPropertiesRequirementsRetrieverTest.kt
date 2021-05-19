package com.epam.brn.service.statistic.progress.status.requirements.impl

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.env.Environment
import javax.naming.OperationNotSupportedException
import kotlin.test.assertEquals

/**
 * @author Nikolai Lazarev
 */

@ExtendWith(MockKExtension::class)
internal class ApplicationPropertiesRequirementsRetrieverTest {

    @InjectMockKs
    private lateinit var retrieverApplicationProperties: ApplicationPropertiesRequirementsRetriever

    @MockK
    private lateinit var env: Environment
    private val basePath = "brn.statistic.progress"

    @Test
    fun `getRequirementsForStatus should return requirements for BAD status`() {
        // GIVEN
        val status = UserExercisingProgressStatus.BAD
        val period = UserExercisingPeriod.DAY
        val minimalRequirements = 0
        val maximalRequirements = 15
        val periodName = period.name.toLowerCase()
        val statusName = status.name.toLowerCase()
        every { env.getProperty("$basePath.$periodName.status.$statusName.maximal") } returns maximalRequirements.toString()
        every { env.getProperty("$basePath.$periodName.status.$statusName.minimal") } returns minimalRequirements.toString()
        val expectedRequirements = StatusRequirements(
            status = status,
            minimalRequirements = minimalRequirements,
            maximalRequirements = maximalRequirements
        )

        // WHEN
        val requirementsForStatus = retrieverApplicationProperties.getRequirementsForStatus(status, period)

        // THEN
        assertEquals(expectedRequirements, requirementsForStatus)
    }

    @Test
    fun `getRequirementsForStatus should throw OperationNotSupportedException when unsupported status passed`() {
        // GIVEN
        val period = UserExercisingPeriod.DAY
        val status = UserExercisingProgressStatus.GOOD
        val maximalRequirements = 15
        val periodName = period.name.toLowerCase()
        val statusName = status.name.toLowerCase()
        every { env.getProperty("$basePath.$periodName.status.$statusName.maximal") } returns maximalRequirements.toString()
        every { env.getProperty("$basePath.$periodName.status.$statusName.minimal") } returns null

        // THEN
        assertThrows<OperationNotSupportedException> {
            retrieverApplicationProperties.getRequirementsForStatus(status, period)
        }
    }
}
