package com.epam.brn.repo

import com.epam.brn.model.UserAnalytics
import com.epam.brn.model.projection.UsersWithAnalyticsView
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserAnalyticsRepository : JpaRepository<UserAnalytics, Long> {
    @Query(
        """
   select     u.id as id, 
              u.userId as userId, 
              u.fullName as fullName,
              u.email as email,
              u.bornYear as bornYear, 
              u.gender as gender,
              u.active as active, 
              u.lastVisit as lastVisit,  
              a.firstDone as firstDone,
              a.lastDone as lastDone,
              a.doneExercises as doneExercises, 
              a.spentTime as spentTime,
              a.studyDays as studyDays
     from UserAnalytics a 
     join UserAccount u on a.userId = u.id  
     where a.roleName=:roleName
    """,
    )
    fun getUserAnalytics(
        pageable: Pageable,
        roleName: String,
    ): List<UsersWithAnalyticsView>
}
