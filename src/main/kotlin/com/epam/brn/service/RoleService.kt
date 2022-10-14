package com.epam.brn.service

import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.model.Role

interface RoleService {
    fun findById(id: Long): Role
    fun findByName(name: String): Role
    fun findAll(): List<Role>
    fun save(role: Role): Role
    fun isUserHasRole(role: String): Boolean
    fun isUserHasRole(user: UserAccountResponse, role: String): Boolean
}
