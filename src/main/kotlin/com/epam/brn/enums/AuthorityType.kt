package com.epam.brn.enums

enum class AuthorityType(val roleName: String) {
    ROLE_ADMIN(RoleConstants.ADMIN),
    ROLE_USER(RoleConstants.USER),
    ROLE_DOCTOR(RoleConstants.DOCTOR);
}

class RoleConstants {
    companion object {
        const val ADMIN = "ADMIN"
        const val USER = "USER"
        const val DOCTOR = "DOCTOR"
    }
}
