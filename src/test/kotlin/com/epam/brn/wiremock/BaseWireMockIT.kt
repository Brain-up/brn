package com.epam.brn.wiremock

import org.junit.jupiter.api.Tag
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles

@Tag("integration-test")
@ActiveProfiles("integration-tests")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseWireMockIT
