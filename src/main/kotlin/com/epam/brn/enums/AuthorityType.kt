package com.epam.brn.enums

enum class AuthorityType(val roleName: String) {
    ROLE_ADMIN(BrnRole.ADMIN),
    ROLE_USER(BrnRole.USER),
    ROLE_SPECIALIST(BrnRole.SPECIALIST);
}

class BrnRole {
    companion object {
        const val ADMIN = "ADMIN"
        const val USER = "USER"
        const val SPECIALIST = "SPECIALIST"
    }
}
