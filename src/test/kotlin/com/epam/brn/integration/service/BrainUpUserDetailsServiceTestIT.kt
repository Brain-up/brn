package com.epam.brn.integration.service

import com.epam.brn.integration.BaseIT
import com.epam.brn.enums.BrnGender
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.BrainUpUserDetailsService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.Objects.nonNull
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class BrainUpUserDetailsServiceTestIT : BaseIT() {

    @Autowired
    private lateinit var brainUpUserDetailsService: BrainUpUserDetailsService

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @BeforeEach
    fun setUp() {
        val userAccount = UserAccount(
            id = null,
            fullName = "testUserFirstName",
            gender = BrnGender.MALE.toString(),
            bornYear = 2000,
            email = "default@gmail.com",
            active = true
        )
        val userAccount1 = UserAccount(
            id = null,
            fullName = "testUserFirstName",
            gender = BrnGender.MALE.toString(),
            bornYear = 2000,
            email = "default1@gmail.com",
            active = true
        )
        val userAccount2 = UserAccount(
            id = null,
            fullName = "testUserFirstName",
            gender = BrnGender.MALE.toString(),
            bornYear = 2000,
            email = "default2@gmail.com",
            active = true
        )
        userAccountRepository.saveAll(listOf(userAccount, userAccount1, userAccount2))
    }

    @AfterEach
    fun deleteAfterTest() {
        userAccountRepository.deleteAll()
    }

    @ParameterizedTest(name = "Method with userEmail {0} should return valid UserDetails or throw expected exception {1} with message {2}")
    @MethodSource("getUserEmailsAndAccountData")
    fun loadUserByUsername(sourceUserEmail: String, ex: Class<out Exception>?, exMessage: String?) {
        if (nonNull(ex)) {
            val actualException = Assertions.assertThrows(ex) {
                brainUpUserDetailsService.loadUserByUsername(sourceUserEmail)
            }
            assertEquals(actualException.message, exMessage)
            return
        }
        val userDetails: UserDetails = brainUpUserDetailsService.loadUserByUsername(sourceUserEmail)
        assertNotNull(userDetails)
        assertEquals(userDetails.username, sourceUserEmail)
    }

    companion object {
        @JvmStatic
        private fun getUserEmailsAndAccountData() = listOf(
            Arguments.of("default@gmail.com", null, null),
            Arguments.of("default1@gmail.com", null, null),
            Arguments.of("default2@gmail.com", null, null),
            Arguments.of(
                "missed@g.com",
                UsernameNotFoundException::class.java,
                "User with email: missed@g.com doesn't exist"
            )
        )
    }
}
