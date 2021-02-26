package com.epam.brn.repo

import com.epam.brn.model.Audiometry
import com.epam.brn.model.AudiometryHistory
import com.epam.brn.model.UserAccount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AudiometryHistoryRepository : JpaRepository<AudiometryHistory, Long> {
    @Query(
        "select distinct ah from AudiometryHistory ah left JOIN FETCH ah.audiometryTask where ah.userAccount = ?1 and ah.audiometryTask.audiometry=?2"
    )
    fun findByUserAndAudiometry(user: UserAccount, audiometry: Audiometry): List<AudiometryHistory>
}
