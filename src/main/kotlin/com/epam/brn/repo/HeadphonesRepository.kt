package com.epam.brn.repo

import com.epam.brn.model.Headphones
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface HeadphonesRepository : JpaRepository<Headphones, Long> {

    @Query(
        "SELECT headphones FROM Headphones headphones WHERE headphones.userAccount.id = :userId"
    )
    fun getHeadphonesForUser(@Param("userId") userId: Long): List<Headphones>
}
