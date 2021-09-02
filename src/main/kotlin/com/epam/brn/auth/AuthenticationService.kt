package com.epam.brn.auth

import com.epam.brn.dto.request.UserAccountAdditionalInfoRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountResponse

interface AuthenticationService {
    fun registration(userAccountCreateRequest: UserAccountCreateRequest): UserAccountResponse?
    fun isNewUser(uuid: String): Boolean
    fun addAdditionalInfo(additionalInfoRequest: UserAccountAdditionalInfoRequest): UserAccountResponse?
}
