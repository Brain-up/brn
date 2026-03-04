package com.epam.brn.integration

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("integration-test")
class SimpleIntegrationTest {
    @Test
    fun `test should pass`() {
        assertTrue(true)
    }
}
