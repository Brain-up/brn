package com.epam.brn.service.impl

import com.epam.brn.dto.UserAccountDto
import com.epam.brn.enums.BrnRole
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Role
import com.epam.brn.repo.RoleRepository
import com.epam.brn.service.RoleService
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Service
class RoleServiceImpl(
    private val roleRepository: RoleRepository,
) : RoleService {
    override fun findById(id: Long): Role = roleRepository
        .findById(id)
        .orElseThrow { EntityNotFoundException("Role with id = $id is not found") }

    override fun findByName(name: String): Role = roleRepository
        .findByName(name)
        ?: throw EntityNotFoundException("Role with name = $name is not found")

    override fun save(role: Role) = roleRepository.save(role)

    override fun isCurrentUserAdmin(): Boolean {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        return request.isUserInRole(BrnRole.ADMIN)
    }

    override fun isUserHasRole(
        user: UserAccountDto,
        role: String,
    ): Boolean = user.roles.contains(role)

    override fun findAll(): List<Role> = roleRepository.findAll()
}
