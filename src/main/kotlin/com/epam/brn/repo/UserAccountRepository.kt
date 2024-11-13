package com.epam.brn.repo

import com.epam.brn.model.UserAccount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Optional
import org.springframework.data.repository.query.Param

@Repository
interface UserAccountRepository : JpaRepository<UserAccount, Long> {

    @Query(
        """select u FROM UserAccount u 
            JOIN FETCH u.roleSet 
            LEFT JOIN FETCH u.headphones 
            where u.fullName = :fullName"""
    )
    fun findUserAccountByName(@Param("fullName") fullName: String): Optional<UserAccount>

    @Query(
        """select u FROM UserAccount u 
            JOIN FETCH u.roleSet 
            LEFT JOIN FETCH u.headphones 
            where LOWER(u.email) = LOWER(:email)"""
    )
    fun findUserAccountByEmail(@Param("email") email: String): Optional<UserAccount>

    @Query(
        """select u FROM UserAccount u 
            JOIN FETCH u.roleSet 
            LEFT JOIN FETCH u.headphones 
            where u.id = :id"""
    )
    fun findUserAccountById(@Param("id") id: Long): Optional<UserAccount>

    @Query(
        """select u FROM UserAccount u 
            JOIN FETCH u.roleSet 
            LEFT JOIN FETCH u.headphones 
            where u.doctor = :doctor"""
    )
    fun findUserAccountsByDoctor(@Param("doctor") doctor: UserAccount): List<UserAccount>

    @Query(
        """select u FROM UserAccount u 
            JOIN FETCH u.roleSet 
            LEFT JOIN FETCH u.headphones 
            where u.doctor.id = :doctorId"""
    )
    fun findUserAccountsByDoctorId(@Param("doctorId") doctorId: Long): List<UserAccount>

    fun findByUserId(uuid: String): UserAccount?

    fun findAllByUserIdIsNullAndIsFirebaseErrorIsFalse(pageable: Pageable): Page<UserAccount>

    @Query(
        """select u FROM UserAccount u 
            JOIN FETCH u.roleSet roles 
            LEFT JOIN FETCH u.headphones 
            where roles.name = :roleName"""
    )
    fun findUsersAccountsByRole(@Param("roleName") roleName: String): List<UserAccount>

    @Transactional
    @Modifying
    @Query(
        """update UserAccount u SET u.lastVisit = :lastVisit
            where u.email = :email"""
    )
    fun updateLastVisitByEmail(email: String, lastVisit: LocalDateTime)

    @Transactional
    fun deleteUserAccountsByEmailStartsWith(prefix: String): Long

    @Transactional
    fun deleteUserAccountByEmailIs(email: String): Long
}
