package com.epam.brn.repo

import com.epam.brn.model.UserDetails
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserDetailsRepository : CrudRepository<UserDetails, Long> {

    fun findByNameLike(name: String): List<UserDetails>
    fun findByIdLike(id: String): List<UserDetails>
}