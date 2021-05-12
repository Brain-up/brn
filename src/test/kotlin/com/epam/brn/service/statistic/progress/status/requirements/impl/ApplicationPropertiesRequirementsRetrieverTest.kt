package com.epam.brn.service.statistic.progress.status.requirements.impl

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.env.Environment
import javax.naming.OperationNotSupportedException
import kotlin.test.assertEquals

/**
 * @author Nikolai Lazarev
 */

@ExtendWith(MockitoExtension::class)
internal class ApplicationPropertiesRequirementsRetrieverTest {

    @InjectMocks
    private lateinit var retrieverApplicationProperties: ApplicationPropertiesRequirementsRetriever

    @Mock
    private lateinit var env: Environment
    private val basePath = "brn.statistic.progress"

    @Test
    fun `getRequirementsForStatus should return requirements for BAD status`() {
        val status = UserExercisingProgressStatus.BAD
        val period = UserExercisingPeriod.DAY
        val minimalRequirements = 0
        val maximalRequirements = 15
        `when`(env.getProperty("$basePath.${period.name}.status.${status.name}.maximal")).thenReturn(maximalRequirements.toString())
        `when`(env.getProperty("$basePath.${period.name}.status.${status.name}.minimal")).thenReturn(minimalRequirements.toString())
        val expectedRequirements = StatusRequirements(
            status = status,
            minimalRequirements = minimalRequirements,
            maximalRequirements = maximalRequirements
        )

        val requirementsForStatus = retrieverApplicationProperties.getRequirementsForStatus(status, period)

        assertEquals(expectedRequirements, requirementsForStatus)
    }

    @Test
    fun `getRequirementsForStatus should throw OperationNotSupportedException when unsupported status passed`() {
        val period = UserExercisingPeriod.DAY
        val status = UserExercisingProgressStatus.GOOD
        val maximalRequirements = 15
        `when`(env.getProperty("$basePath.${period.name}.status.${status.name}.maximal")).thenReturn(maximalRequirements.toString())
        `when`(env.getProperty("$basePath.${period.name}.status.${status.name}.minimal")).thenReturn(null)

        assertThrows<OperationNotSupportedException> {
            retrieverApplicationProperties.getRequirementsForStatus(status, period)
        }
    }
}
