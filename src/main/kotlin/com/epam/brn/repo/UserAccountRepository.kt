package com.epam.brn.repo

import com.epam.brn.model.UserAccount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserAccountRepository : JpaRepository<UserAccount, Long> {

    @Query("select DISTINCT u FROM UserAccount u left JOIN FETCH u.authoritySet left JOIN FETCH u.headphones where u.fullName = ?1 ")
    fun findUserAccountByName(fullName: String): Optional<UserAccount>

    @Query("select DISTINCT u FROM UserAccount u left JOIN FETCH u.authoritySet left JOIN FETCH u.headphones where LOWER(u.email) = LOWER( ?1)")
    fun findUserAccountByEmail(email: String): Optional<UserAccount>

    @Query("select DISTINCT u FROM UserAccount u left JOIN FETCH u.authoritySet left JOIN FETCH u.headphones where u.id = ?1")
    fun findUserAccountById(id: Long): Optional<UserAccount>

    @Query("select DISTINCT u FROM UserAccount u left JOIN FETCH u.authoritySet left JOIN FETCH u.headphones where u.doctor = ?1")
    fun findUserAccountsByDoctor(doctor: UserAccount): List<UserAccount>

    @Query("select DISTINCT u FROM UserAccount u left JOIN FETCH u.authoritySet left JOIN FETCH u.headphones where u.doctor.id = ?1")
    fun findUserAccountsByDoctorId(doctorId: Long): List<UserAccount>

    fun findByUserId(uuid: String): UserAccount?

    fun findAllByUserIdIsNull(pageable: Pageable): Page<UserAccount>

    @Query("select DISTINCT u FROM UserAccount u left JOIN FETCH u.authoritySet authorities left JOIN FETCH u.headphones where authorities.authorityName = :authorityName")
    fun findUsersAccountsByRole(authorityName: String): List<UserAccount>
}
