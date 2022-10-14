package com.epam.brn.service.impl

import com.epam.brn.dto.response.UserAccountResponse
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
        log.debug("getting the authority with authorityId=$id")
        return roleRepository.findById(id).orElseThrow { EntityNotFoundException("Role with id = $id is not found") }
    }

    override fun findByName(name: String): Role {
        log.debug("getting the authority with authorityName=$name")
        return roleRepository.findByName(name)
            ?: throw EntityNotFoundException("Role with name = $name is not found")
    }

    override fun save(role: Role) = roleRepository.save(role)

    override fun isUserHasRole(role: String): Boolean {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request
        return request.isUserInRole(role)
    }

    override fun isUserHasRole(user: UserAccountResponse, role: String) = user.roles?.contains(role) ?: false

    override fun findAll(): List<Role> = roleRepository.findAll()
}
