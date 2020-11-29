package com.epam.brn.integration

import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
@Testcontainers
abstract class BaseIT {
    @Container
    var postgreSQLContainer = BrnPostgresqlContainer.instance

    @Autowired
    lateinit var mockMvc: MockMvc
}

class BrnPostgresqlContainer private constructor() :
    PostgreSQLContainer<BrnPostgresqlContainer?>(IMAGE_VERSION) {
    override fun start() {
        super.start()
        System.setProperty("DB_URL", container!!.jdbcUrl)
        System.setProperty("DB_USERNAME", container!!.username)
        System.setProperty("DB_PASSWORD", container!!.password)
    }
    override fun stop() {
        // do nothing, JVM handles shut down
    }
    companion object {
        private const val IMAGE_VERSION = "postgres:13"
        private var container: BrnPostgresqlContainer? = null
        val instance: BrnPostgresqlContainer?
            get() {
                if (container == null)
                    container = BrnPostgresqlContainer()
                return container
            }
    }
}
