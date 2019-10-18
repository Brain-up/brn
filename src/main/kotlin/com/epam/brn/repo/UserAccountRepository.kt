package com.epam.brn.repo

import com.epam.brn.model.UserAccount
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserAccountRepository : CrudRepository<UserAccount, Long> {

    fun findByIdLike(id: String): List<UserAccount>
}