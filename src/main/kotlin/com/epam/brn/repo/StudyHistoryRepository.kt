package com.epam.brn.repo

import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface StudyHistoryRepository : CrudRepository<StudyHistory, Long> {
    @Query("SELECT DISTINCT s.exercise.id FROM StudyHistory s " +
                " WHERE s.exercise.series.id = :seriesId and s.userAccount.id = :userId"
    )
    fun getDoneExercisesIdList(@Param("seriesId") seriesId: Long, @Param("userId") userId: Long): List<Long>

    @Query("SELECT DISTINCT s.exercise FROM StudyHistory s " +
                " WHERE s.exercise.series.id = :seriesId and s.userAccount.id = :userId"
    )
    fun getDoneExercises(@Param("seriesId") seriesId: Long, @Param("userId") userId: Long): List<Exercise>

    @Query("SELECT DISTINCT s.exercise FROM StudyHistory s " +
                " WHERE s.exercise.name = :name and s.userAccount.id = :userId"
    )
    fun getDoneExercisesByName(@Param("name") name: String, @Param("userId") userId: Long): List<Exercise>

    @Query("SELECT DISTINCT s.exercise.id FROM StudyHistory s " +
                " WHERE s.userAccount.id = :userId"
    )
    fun getDoneExercisesIdList(@Param("userId") userId: Long): List<Long>

    fun findByUserAccountIdAndExerciseId(userId: Long, exerciseId: Long): List<StudyHistory>

    @Query("SELECT s FROM StudyHistory s " +
                " WHERE (s.userAccount.id, s.startTime) " +
                " IN (SELECT userAccount.id, max(startTime) " +
                "       FROM StudyHistory " +
                "       GROUP BY exercise.id, userAccount.id " +
                "       HAVING userAccount.id = :userId)"
    )
    fun findLastByUserAccountId(userId: Long): List<StudyHistory>

    @Query("SELECT s FROM StudyHistory s " +
            " WHERE (s.userAccount.id, s.exercise.id, s.startTime) " +
            " IN (SELECT userAccount.id, exercise.id, max(startTime) " +
            "       FROM StudyHistory " +
            "       GROUP BY exercise.id " +
            "       HAVING userAccount.id = :userId and exercise.id in (:exerciseIds))"
    )
    fun findLastByUserAccountIdAndExercises(userId: Long, exerciseIds: List<Long>): List<StudyHistory>

    @Query("SELECT sum(s.executionSeconds) FROM StudyHistory s " +
            " WHERE date_trunc('day', s.startTime) = date_trunc('day', :day) " +
            " AND s.userAccount.id = :userId"
    )
    fun getDayTimer(userId: Long, day: LocalDate): Int
}
