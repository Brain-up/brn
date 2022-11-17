package com.epam.brn.integration

import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.enums.BrnRole
import com.epam.brn.integration.firebase.FirebaseWebClientTestMock
import com.epam.brn.integration.firebase.model.FirebaseVerifyPasswordRequest
import com.epam.brn.model.Role
import com.epam.brn.enums.BrnGender
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.RoleRepository
import com.epam.brn.repo.UserAccountRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthorizationAuthenticationIT : BaseIT() {

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var firebaseAuth: FirebaseAuth

    @Autowired
    lateinit var firebaseWebClientTestMock: FirebaseWebClientTestMock

    internal val baseUrl = "/groups"
    internal val searchUsersPath = "/users"

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

        val roleUserName = BrnRole.USER
        val userRole = roleRepository.findByName(roleUserName)
            ?: roleRepository.save(Role(name = roleUserName))

        val roleName = BrnRole.ADMIN
        val adminRole = roleRepository.findByName(roleName)
            ?: roleRepository.save(Role(name = roleName))
        createUserInLocalDatabase(fullName, email, uuidFirebaseAdmin, adminRole)
        createUserInLocalDatabase(userRoleFullName, userRoleEmail, uuidFirebaseUserRole, userRole)
    }

    @AfterEach
    fun deleteAfterTest() {
        userAccountRepository.deleteAll()
        roleRepository.deleteAll()
        deleteFirebaseUser(uuidFirebaseAdmin)
        deleteFirebaseUser(uuidFirebaseNewUser)
        deleteFirebaseUser(uuidFirebaseUserRole)
    }

    @Test
    fun `test get groups authentication`() {
        val verifyPasswordResponse =
            firebaseWebClientTestMock.verifyPassword(FirebaseVerifyPasswordRequest(userRoleEmail, userRolePassword, true))
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
        resultAction.andExpect(status().isForbidden)
    }

    @Test
    fun `test create new user in local DB when login new firebase user`() {
        val verifyPasswordResponse =
            firebaseWebClientTestMock.verifyPassword(FirebaseVerifyPasswordRequest(newUserEmail, newUserPassword, true))
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
            firebaseWebClientTestMock.verifyPassword(FirebaseVerifyPasswordRequest(userRoleEmail, userRolePassword, true))
        val idToken = "Bearer ${verifyPasswordResponse?.idToken}"

        // WHEN
        val resultAction = this.mockMvc
            .perform(
                get(searchUsersPath).header("Authorization", idToken)
            )
        // THEN
        resultAction.andExpect(status().isForbidden)
    }

    private fun saveFirebaseUser(fullName: String, email: String, password: String): UserRecord? {
        val firebaseUser = UserAccountCreateRequest(
            name = fullName,
            email = email,
            password = password,
            gender = BrnGender.MALE,
            bornYear = 2000
        )
        try {
            val userByEmail = firebaseAuth.getUserByEmail(firebaseUser.email)
            if (userByEmail?.uid != null) {
                firebaseAuth.deleteUser(userByEmail.uid)
            }
        } catch (e: Exception) {
        }
        return addFirebaseUser(firebaseUser)
    }

    private fun createUserInLocalDatabase(fullName: String, email: String, uuid: String, role: Role) {
        val userAccount =
            UserAccount(
                fullName = fullName,
                email = email,
                gender = BrnGender.MALE.toString(),
                bornYear = 2000,
                active = true,
                userId = uuid
            )
        userAccount.roleSet.add(role)
        userAccountRepository.save(userAccount)
    }

    fun addFirebaseUser(userAccountCreateRequest: UserAccountCreateRequest): UserRecord? {
        val firebaseUser = UserRecord.CreateRequest()
            .setEmail(userAccountCreateRequest.email)
            .setDisplayName(userAccountCreateRequest.name)
            .setPassword(userAccountCreateRequest.password)
            .setEmailVerified(false)
        if (userAccountCreateRequest.avatar != null) {
            firebaseUser
                .setPhotoUrl(userAccountCreateRequest.avatar)
        }
        val createdUser = firebaseAuth.createUser(firebaseUser)
        return createdUser
    }

    fun deleteFirebaseUser(uuid: String) {
        firebaseAuth.deleteUser(uuid)
    }
}
