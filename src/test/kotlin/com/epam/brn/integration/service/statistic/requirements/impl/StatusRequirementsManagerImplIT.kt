package com.epam.brn.integration.service.statistic.requirements.impl

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.integration.BaseIT
import com.epam.brn.service.statistic.progress.status.requirements.impl.StatusRequirementsManagerImpl
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals

/**
 *@author Nikolai Lazarev
 */

class StatusRequirementsManagerImplIT : BaseIT() {

    @Autowired
    private lateinit var manager: StatusRequirementsManagerImpl

    private val periodRequirementsWeek = listOf(
        StatusRequirements(UserExercisingProgressStatus.BAD, 0, 5),
        StatusRequirements(UserExercisingProgressStatus.GOOD, 5, 6),
        StatusRequirements(UserExercisingProgressStatus.GREAT, 6, 8)
    )

    private val periodRequirementsDay = listOf(
        StatusRequirements(UserExercisingProgressStatus.BAD, 0, 15),
        StatusRequirements(UserExercisingProgressStatus.GOOD, 15, 20),
        StatusRequirements(UserExercisingProgressStatus.GREAT, 20, 60 * 24)
    )

    @Test
    fun `getPeriodRequirements should return all requirements for WEEK period`() {
        // WHEN
        val periodRequirements = manager.getPeriodRequirements(UserExercisingPeriod.WEEK)

        // THEN
        assertEquals(periodRequirementsWeek, periodRequirements)
    }

    @Test
    fun `getPeriodRequirements should return all requirements for DAY period`() {
        // WHEN
        val periodRequirements = manager.getPeriodRequirements(UserExercisingPeriod.DAY)

        // THEN
        assertEquals(periodRequirementsDay, periodRequirements)
    }
}
