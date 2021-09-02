package com.epam.brn.service

import org.springframework.util.StringUtils
import javax.servlet.http.HttpServletRequest

class TokenHelperUtils {

    companion object {
        @JvmStatic
        fun getBearerToken(request: HttpServletRequest): String? {
            var bearerToken: String? = null
            val authorization = request.getHeader("Authorization")
            if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
                bearerToken = authorization.substring(7)
            }
            return bearerToken
        }
    }
}
