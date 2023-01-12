package com.epam.brn.service.impl

import com.epam.brn.dto.UserAccountDto
import com.epam.brn.enums.BrnRole
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Role
import com.epam.brn.repo.RoleRepository
import com.epam.brn.service.RoleService
import org.apache.logging.log4j.kotlin.logger
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder

import org.springframework.web.context.request.ServletRequestAttributes

@Service
class RoleServiceImpl(private val roleRepository: RoleRepository) : RoleService {

    private val log = logger()

    override fun findById(id: Long): Role {
        log.debug("getting the role with roleId=$id")
        return roleRepository.findById(id).orElseThrow { EntityNotFoundException("Role with id = $id is not found") }
    }

    override fun findByName(name: String): Role {
        log.debug("getting the role with roleName=$name")
        return roleRepository.findByName(name)
            ?: throw EntityNotFoundException("Role with name = $name is not found")
    }

    override fun save(role: Role) = roleRepository.save(role)

    override fun isCurrentUserAdmin(): Boolean {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        return request.isUserInRole(BrnRole.ADMIN)
    }

    override fun isCurrentUserSpecialist(): Boolean {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        return request.isUserInRole(BrnRole.SPECIALIST)
    }

    override fun isUserHasRole(user: UserAccountDto, role: String) = user.roles?.contains(role) ?: false

    override fun findAll(): List<Role> = roleRepository.findAll()
}
