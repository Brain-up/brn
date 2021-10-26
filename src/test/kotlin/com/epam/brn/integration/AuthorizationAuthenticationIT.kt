package com.epam.brn.integration

import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.integration.firebase.FirebaseWebClient
import com.epam.brn.integration.firebase.model.FirebaseVerifyPasswordRequest
import com.epam.brn.enums.Role
import com.epam.brn.model.Authority
import com.epam.brn.model.Gender
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.FirebaseUserService
import com.google.firebase.auth.UserRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthorizationAuthenticationIT : BaseIT() {

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var authorityRepository: AuthorityRepository

    @Autowired
    lateinit var firebaseUserService: FirebaseUserService

    @Autowired
    lateinit var firebaseWebClient: FirebaseWebClient

    internal val baseUrl = "/groups"
    internal val adminUsersPath = "/admin/users"

    internal val email: String = "testAdmin@admin.com"
    internal val fullName = "testUserFirstName"
    internal val password: String = "testAdmin"
    internal var uuidFirebaseAdmin = ""

    internal val newUserEmail = "newuser@mail.com"
    internal val newUserFullName = "NewUSER"
    internal val newUserPassword = "password"
    internal var uuidFirebaseNewUser = ""

    internal val userRoleEmail = "user_role@mail.com"
    internal val userRoleFullName = "USER_ROLE"
    internal val userRolePassword = "password"
    internal var uuidFirebaseUserRole = ""

    @BeforeEach
    fun initBeforeEachTest() {
        uuidFirebaseAdmin = saveFirebaseUser(fullName, email, password)!!.uid
        uuidFirebaseNewUser = saveFirebaseUser(newUserFullName, newUserEmail, newUserPassword)!!.uid
        uuidFirebaseUserRole = saveFirebaseUser(userRoleFullName, userRoleEmail, userRolePassword)!!.uid

        val roleUserName = Role.ROLE_ADMIN.name
        val userAuthority = authorityRepository.findAuthorityByAuthorityName(roleUserName)
            ?: authorityRepository.save(Authority(authorityName = roleUserName))

        val authName = "ROLE_ADMIN"
        val adminAuthority = authorityRepository.findAuthorityByAuthorityName(authName)
            ?: authorityRepository.save(Authority(authorityName = authName))
        createUserInLocalDatabase(fullName, email, uuidFirebaseAdmin, adminAuthority)
        createUserInLocalDatabase(userRoleFullName, userRoleEmail, uuidFirebaseUserRole, userAuthority)
    }

    @AfterEach
    fun deleteAfterTest() {
        userAccountRepository.deleteAll()
        authorityRepository.deleteAll()
        firebaseUserService.deleteUser(uuidFirebaseAdmin)
        firebaseUserService.deleteUser(uuidFirebaseNewUser)
        firebaseUserService.deleteUser(uuidFirebaseUserRole)
    }

    @Test
    fun `test get groups with valid credentials`() {
        // WHEN
        val resultAction = this.mockMvc.perform(
            get(baseUrl)
                .with(user(this.email).password(this.password).roles("USER", "ADMIN"))
        )
        // THEN
        resultAction.andExpect(status().isOk)
    }

    @Test
    fun `test get groups authentication`() {
        val verifyPasswordResponse =
            firebaseWebClient.verifyPassword(FirebaseVerifyPasswordRequest(email, password, true))
        val idToken = "Bearer ${verifyPasswordResponse?.idToken}"
        // WHEN
        val resultAction = this.mockMvc
            .perform(
                get(baseUrl).header("Authorization", idToken)
            )
        // THEN
        resultAction.andExpect(status().isOk)
    }

    @Test
    fun `test get groups authentication invalid token`() {
        val badToken =
            "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6ImYwNTM4MmFlMTgxYWJlNjFiOTYwYjA1Yzk3ZmE0MDljNDdhNDQ0ZTciLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoidGVzdFVzZXJGaXJzdE5hbWUiLCJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vYnJhaW5hcHAta295bHViYWV2IiwiYXVkIjoiYnJhaW5hcHAta295bHViYWV2IiwiYXV0aF90aW1lIjoxNjM0MTE2MjM4LCJ1c2VyX2lkIjoiZmVRRGRMTVRkU1F2UnVNVTNjeUNmRmt6dFNRMiIsInN1YiI6ImZlUURkTE1UZFNRdlJ1TVUzY3lDZkZrenRTUTIiLCJpYXQiOjE2MzQxMTYyMzgsImV4cCI6MTYzNDExOTgzOCwiZW1haWwiOiJ0ZXN0YWRtaW5AYWRtaW4uY29tIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJmaXJlYmFzZSI6eyJpZGVudGl0aWVzIjp7ImVtYWlsIjpbInRlc3RhZG1pbkBhZG1pbi5jb20iXX0sInNpZ25faW5fcHJvdmlkZXIiOiJwYXNzd29yZCJ9fQ.PYLm9Zlo1edRmLWCK-RxW_GJ0oRtDYS4QHUV8SZ5rjG6R0AQucT8DcZD6Qlp0BOKLYzDrO3kEq5vs6e3FAvX-x7FT2hRUmuWTsWKDuC4sRhJBTM-zgP01BiqcTQ1vN23bZ70FW98BdhnPuLVDmB9wrDtVfDs4Zj3RsxOQGwPwyNr6FXJW0P9s55gnD5rFGr_2lNQRAIlOlTKKrYOboo1TqFYVUXcuY6GqDcKUkRGfr0sLdRorYCSZVGkjenyFCllIlMeIrTkbnWUtanKIHorwdMtmYTcneMV6bAMmOMvOQBA9lHH8pqeaDjHR_GxgzyXevEo74E2SxatGSUZ0lQK2Q"
        // WHEN
        val resultAction = this.mockMvc
            .perform(
                get(baseUrl).header("Authorization", badToken)
            )
        // THEN
        resultAction.andExpect(status().isUnauthorized)
    }

    @Test
    fun `test create new user in local DB when login new firebase user`() {
        val verifyPasswordResponse =
            firebaseWebClient.verifyPassword(FirebaseVerifyPasswordRequest(newUserEmail, newUserPassword, true))
        val idToken = "Bearer ${verifyPasswordResponse?.idToken}"

        // WHEN
        val resultAction = this.mockMvc
            .perform(
                get(baseUrl).header("Authorization", idToken)
            )
        // THEN
        resultAction.andExpect(status().isOk)
        val userAccount = userAccountRepository.findUserAccountByEmail(newUserEmail).get()
        assertNotNull(userAccount)
        assertEquals(newUserFullName, userAccount.fullName)
    }

    @Test
    fun `test get admin-users when don't have permission for it`() {
        val verifyPasswordResponse =
            firebaseWebClient.verifyPassword(FirebaseVerifyPasswordRequest(userRoleEmail, userRolePassword, true))
        val idToken = "Bearer ${verifyPasswordResponse?.idToken}"

        // WHEN
        val resultAction = this.mockMvc
            .perform(
                get(adminUsersPath).header("Authorization", idToken)
            )
        // THEN
        resultAction.andExpect(status().isForbidden)
    }

    private fun saveFirebaseUser(fullName: String, email: String, password: String): UserRecord? {
        val firebaseUser = UserAccountCreateRequest(
            name = fullName,
            email = email,
            password = password,
            gender = Gender.MALE,
            bornYear = 2000
        )
        try {
            val userByEmail = firebaseUserService.getUserByEmail(firebaseUser.email)
            if (userByEmail?.uid != null) {
                firebaseUserService.deleteUser(userByEmail.uid)
            }
        } catch (e: Exception) {
        }
        return firebaseUserService.addUser(firebaseUser)
    }

    private fun createUserInLocalDatabase(fullName: String, email: String, uuid: String, authority: Authority) {
        val userAccount =
            UserAccount(
                fullName = fullName,
                email = email,
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                active = true,
                userId = uuid
            )
        userAccount.authoritySet.add(authority)
        userAccountRepository.save(userAccount)
    }
}
