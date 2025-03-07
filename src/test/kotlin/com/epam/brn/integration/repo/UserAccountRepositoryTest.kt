package com.epam.brn.integration.repo

import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@DataJpaTest
@Tag("integration-test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserAccountRepositoryTest {

    @Autowired
    private lateinit var repository: UserAccountRepository

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Test
    fun `should update lastVisit date of user`() {
        // GIVEN
        val yesterday = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MILLIS)
        val today = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)

        val email = "test@email.com"
        val user = UserAccount(
            email = email,
            fullName = "John Doe",
            lastVisit = yesterday
        )
        val savedUser = testEntityManager.persistAndFlush(user)

        // WHEN
        repository.updateLastVisitByEmail(email, today)

        testEntityManager.flush()
        testEntityManager.clear()

        // THEN
        val retrievedUser = testEntityManager.find(UserAccount::class.java, savedUser.id)
        assertThat(retrievedUser).isNotNull
        val actualLastVisit = retrievedUser.lastVisit?.truncatedTo(ChronoUnit.MILLIS)
        assertThat(actualLastVisit).isEqualTo(today)
    }

    @Test
    fun `should not throw error when user doesn't exist`() {
        // GIVEN
        val today = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val email = "404.test@email.com"

        // WHEN & THEN
        assertDoesNotThrow {
            repository.updateLastVisitByEmail(email, today)
        }
    }
}
