package com.epam.brn.integration

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("integration-tests")
@SpringBootTest
@Tag("integration-test")
internal class MainIT {
    @Test
    fun contextLoads() {
        // main simple test that context is load
    }
}
