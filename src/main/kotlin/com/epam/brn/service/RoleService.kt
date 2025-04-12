package com.epam.brn.service

import com.epam.brn.dto.UserAccountDto
import com.epam.brn.model.Role

interface RoleService {
    fun findById(id: Long): Role
    fun findByName(name: String): Role
    fun findAll(): List<Role>
    fun save(role: Role): Role
    fun isCurrentUserAdmin(): Boolean
    fun isUserHasRole(
        user: UserAccountDto,
        role: String,
    ): Boolean
}
