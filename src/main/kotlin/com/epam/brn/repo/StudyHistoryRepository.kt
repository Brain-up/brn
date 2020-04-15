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
                " WHERE s.exercise.series.id = :seriesId and s.userId = :userId"
    )
    fun getDoneExercisesIdList(@Param("seriesId") seriesId: Long, @Param("userId") userId: String): List<Long>

    @Query(
        value = "SELECT s.exercise.id FROM StudyHistory s " +
                " WHERE s.userId = :userId"
    )

    fun getDoneExercisesIdList(@Param("userId") userId: String): List<Long>

    fun findByUserIdAndExerciseId(userId: String?, exerciseId: Long?): Optional<StudyHistory>
}
