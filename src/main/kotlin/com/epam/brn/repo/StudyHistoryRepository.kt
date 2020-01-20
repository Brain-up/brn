package com.epam.brn.repo

import com.epam.brn.model.StudyHistory
import java.util.Optional
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface StudyHistoryRepository : CrudRepository<StudyHistory, Long> {
    @Query(
        value = "SELECT s.exercise.id FROM StudyHistory s " +
                " WHERE s.exercise.series.id = :seriesId and s.userAccount.id = :userId"
    )
    fun getDoneExercisesIdList(@Param("seriesId") seriesId: Long, @Param("userId") userId: Long): List<Long>

    @Query(
        value = "SELECT s.exercise.id FROM StudyHistory s " +
                " WHERE s.userAccount.id = :userId"
    )

    fun getDoneExercisesIdList(@Param("userId") userId: Long): List<Long>
    fun findByUserAccountId(id: Long?): List<StudyHistory>
    fun findByUserAccountIdAndExerciseId(userId: Long?, exerciseId: Long?): Optional<StudyHistory>
}
