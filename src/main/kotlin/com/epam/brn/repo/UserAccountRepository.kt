package com.epam.brn.repo

import com.epam.brn.model.UserAccount
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserAccountRepository : JpaRepository<UserAccount, Long> {

    @Query("select DISTINCT u FROM UserAccount u left JOIN FETCH u.authoritySet where u.userName = ?1")
    fun findUserAccountByUserName(userName: String): Optional<UserAccount>

    @Query("select DISTINCT u FROM UserAccount u left JOIN FETCH u.authoritySet where u.email = ?1")
    fun findUserAccountByEmail(email: String): Optional<UserAccount>

    @Query("select DISTINCT u FROM UserAccount u left JOIN FETCH u.authoritySet where u.id = ?1")
    fun findUserAccountById(id: Long): Optional<UserAccount>
}
