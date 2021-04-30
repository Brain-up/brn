package com.epam.brn.service.statistic.impl.strategy

import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

/**
 * @author Nikolai Lazarev
 */

@ExtendWith(MockitoExtension::class)
internal class DayUserGoalAchievedStrategyTest {

    var dayUserGoalAchievedStrategy: DayUserGoalAchievedStrategy = DayUserGoalAchievedStrategy()

    @Before
    fun init() {
        dayUserGoalAchievedStrategy.defaultMinutesPerDaySuccess = 20
    }

    @Test
    fun `doStrategy should return 20 percent success`() {
        val exercisingMinutes = 5
        val goalProgressInPercentage = dayUserGoalAchievedStrategy.doStrategy(exercisingMinutes * 60)

        assertEquals(25, goalProgressInPercentage)
    }

    @Test
    fun `doStrategy should return 110 percent success when user exceeded the goal`() {
        val exercisingMinutes = 22
        val goalProgressInPercentage = dayUserGoalAchievedStrategy.doStrategy(exercisingMinutes * 60)

        assertEquals(110, goalProgressInPercentage)
    }

    @Test
    fun `doStrategy should return 0 when user did not train`() {
        val exercisingMinutes = 0
        val goalProgressInPercentage = dayUserGoalAchievedStrategy.doStrategy(exercisingMinutes * 60)

        assertEquals(0, goalProgressInPercentage)
    }
}
