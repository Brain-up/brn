package com.epam.brn.auth

import com.epam.brn.dto.request.UserAccountAdditionalInfoRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.service.FirebaseUserService
import com.epam.brn.service.UserAccountService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service

@Service
class AuthenticationBasicServiceImpl(
    private val userAccountService: UserAccountService,
    private val firebaseUserService: FirebaseUserService
) : AuthenticationService {
    private val log = logger()

    override fun registration(userAccountCreateRequest: UserAccountCreateRequest): UserAccountResponse? {
        val userByEmail = firebaseUserService.getUserByEmail(userAccountCreateRequest.email)
        if (userByEmail != null) {
            throw IllegalArgumentException("The user already exists!")
        }
        val firebaseUserRecord = firebaseUserService.addUser(userAccountCreateRequest)
        if (firebaseUserRecord != null) {
            val newUser = userAccountService.createUser(userAccountCreateRequest, firebaseUserRecord)
            log.info("created new user id=${newUser.id}")
        }
        return null
    }

    override fun addAdditionalInfo(userAccountAdditionalInfoRequest: UserAccountAdditionalInfoRequest): UserAccountResponse? {
        val firebaseUserRecord = firebaseUserService.getUserById(userAccountAdditionalInfoRequest.uuid)
        if (firebaseUserRecord?.providerId == "password") {
            // customize error
            throw IllegalArgumentException("Incorrect user account")
        }
        if (firebaseUserRecord != null) {
            return userAccountService.createUserWithFirebase(userAccountAdditionalInfoRequest, firebaseUserRecord)
        }
        return null
    }

    override fun isNewUser(uuid: String): Boolean {
        val user = userAccountService.findUserByUuid(uuid)
        return user == null
    }
}
