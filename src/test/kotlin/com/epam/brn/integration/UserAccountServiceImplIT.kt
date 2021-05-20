package com.epam.brn.integration

import com.epam.brn.auth.AuthorityService
import com.epam.brn.enums.AudiometryType
import com.epam.brn.model.Authority
import com.epam.brn.model.Gender
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.impl.UserAccountServiceImpl
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UserAccountServiceImplIT : BaseIT() {

    @Autowired
    private lateinit var authorityService: AuthorityService

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    private lateinit var userAccountServiceImpl: UserAccountServiceImpl

    @Test
    fun `get userAccounts by roles`() {
        val authoritySet = mutableSetOf(
            authorityService.save(Authority(authorityName = "ROLE_ADMIN")),
            authorityService.save(Authority(authorityName = "ROLE_DOCTOR")),
            authorityService.save(Authority(authorityName = "ROLE_USER"))
        )
        createUsers(authoritySet)
        val test = userAccountServiceImpl.getAllUsersByAuthoritySet(mutableSetOf(authoritySet.elementAt(0)))
        println(test)
        assertTrue { test.isNotEmpty() }
    }

    private fun createUsers(authoritySet: MutableSet<Authority>) {
        val userList = addAdmins(authoritySet)
        userList.addAll(addDoctors(authoritySet))
        userList.addAll(addUsers(authoritySet))
        userAccountRepository.saveAll(userList)
    }

    private fun addAdmins(authoritySet: MutableSet<Authority>): MutableList<UserAccount> {
        val firstAdmin =
            UserAccount(
                fullName = "admin1",
                password = "password",
                email = "admin1@admin.com",
                active = true,
                bornYear = 2001,
                gender = Gender.FEMALE.toString()
            )
        val secondAdmin =
            UserAccount(
                fullName = "admin2",
                password = "password",
                email = "admin2@admin.com",
                active = true,
                bornYear = 2002,
                gender = Gender.MALE.toString()
            )
        firstAdmin.authoritySet.addAll(setOf(authoritySet.elementAt(0)))
        secondAdmin.authoritySet.addAll(setOf(authoritySet.elementAt(0), authoritySet.elementAt(1)))
        return mutableListOf(firstAdmin, secondAdmin)
    }

    private fun addDoctors(authoritySet: MutableSet<Authority>): MutableList<UserAccount> {
        val firstDoctor = UserAccount(
            fullName = "Doctor1",
            email = "doctor1@doctor.ru",
            active = true,
            bornYear = 1981,
            gender = Gender.MALE.toString(),
            password = "password"
        )
        val secondDoctor = UserAccount(
            fullName = "Doctor2",
            email = "doctor2@doctor.ru",
            active = true,
            bornYear = 1982,
            gender = Gender.FEMALE.toString(),
            password = "password"
        )
        firstDoctor.authoritySet.addAll(setOf(authoritySet.elementAt(1)))
        secondDoctor.authoritySet.addAll(setOf(authoritySet.elementAt(1), authoritySet.elementAt(0)))
        return mutableListOf(firstDoctor, secondDoctor)
    }

    private fun addUsers(authoritySet: MutableSet<Authority>): MutableList<UserAccount> {
        val firstUser = UserAccount(
            fullName = "User1",
            email = "user1@user.ru",
            active = true,
            bornYear = 1991,
            gender = Gender.MALE.toString(),
            password = "password"
        )
        val secondUser = UserAccount(
            fullName = "User2",
            email = "user2@user.ru",
            active = true,
            bornYear = 1992,
            gender = Gender.FEMALE.toString(),
            password = "password"
        )
        firstUser.authoritySet.addAll(setOf(authoritySet.elementAt(2)))
        secondUser.authoritySet.addAll(setOf(authoritySet.elementAt(2), authoritySet.elementAt(0)))
        return mutableListOf(firstUser, secondUser)
    }
}
