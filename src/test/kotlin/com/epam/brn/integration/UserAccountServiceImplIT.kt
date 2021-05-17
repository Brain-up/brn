package com.epam.brn.integration

import com.epam.brn.auth.AuthorityService
import com.epam.brn.model.Authority
import com.epam.brn.model.Gender
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.HeadphonesRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.impl.UserAccountServiceImpl
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UserAccountServiceImplIT : BaseIT() {

    @Autowired
    private lateinit var authorityRepository: AuthorityRepository

    @Autowired
    private lateinit var authorityService: AuthorityService

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    private lateinit var userAccountServiceImpl: UserAccountServiceImpl

    @Autowired
    private lateinit var headphonesRepository: HeadphonesRepository

    @Test
    fun someTest() {
        val adminAuthority = authorityService.save(Authority(authorityName = "ROLE_ADMIN"))
        val userAccount = UserAccount(
            fullName = "testUserFirstName",
            email = "test@test.test",
            password = "password",
            gender = Gender.MALE.toString(),
            bornYear = 2000,
            active = true
        )
        userAccount.authoritySet.addAll(setOf(adminAuthority))
        val user = userAccountRepository.save(userAccount)
        println(authorityRepository.findAuthorityByAuthorityName(adminAuthority.authorityName))
        val result = listOf(user)
        val test = userAccountServiceImpl.getAllUsersByAuthorityName("ROLE_ADMIN")
        println(user)
        println(test)
        assertTrue { result == test }
    }
}
