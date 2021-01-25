package com.epam.brn.repo

import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.sql.Date

@Repository
interface StudyHistoryRepository : CrudRepository<StudyHistory, Long> {
//    @Query("SELECT DISTINCT s.exercise.id FROM StudyHistory s " +
//                " WHERE s.exercise.series.id = :seriesId and s.userAccount.id = :userId"
//    )
//    fun getDoneExercisesIdList(@Param("seriesId") seriesId: Long, @Param("userId") userId: Long): List<Long>

    @Query("SELECT DISTINCT s.exercise FROM StudyHistory s " +
                " WHERE s.exercise.subGroup.id = :subGroupId and s.userAccount.id = :userId"
    )
    fun getDoneExercises(@Param("subGroupId") subGroupId: Long, @Param("userId") userId: Long): List<Exercise>

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
            "       GROUP BY exercise.id, userAccount.id " +
            "       HAVING userAccount.id = :userId and exercise.id in (:exerciseIds))"
    )
    fun findLastByUserAccountIdAndExercises(userId: Long, exerciseIds: List<Long>): List<StudyHistory>

    @Query("SELECT COALESCE(sum(s.executionSeconds), 0) FROM StudyHistory s " +
            " WHERE date_trunc('day', s.startTime) = :day" +
            " AND s.userAccount.id = :userId"
    )
    fun getDayTimer(userId: Long, day: java.util.Date): Int

    @Query("SELECT COALESCE(sum(COALESCE(s.executionSeconds, 0)), 0) FROM StudyHistory s " +
            " WHERE date_trunc('day', now()) = date_trunc('day', s.startTime)" +
            " AND s.userAccount.id = :userId"
    )
    fun getTodayDayTimer(userId: Long): Int

    @Query("SELECT s FROM StudyHistory s " +
            " WHERE date_trunc('day', s.startTime) >= :from " +
            " AND date_trunc('day', s.startTime) < :to " +
            " AND s.userAccount.id = :userId"
    )
    fun getHistories(userId: Long, from: Date, to: Date): List<StudyHistory>

    @Query("SELECT s FROM StudyHistory s " +
            " WHERE EXTRACT(MONTH FROM s.startTime) = :month " +
            " AND EXTRACT(YEAR FROM s.startTime) = :year " +
            " AND s.userAccount.id = :userId"
    )
    fun getMonthHistories(userId: Long, month: Int, year: Int): List<StudyHistory>

    @Query("SELECT s FROM StudyHistory s " +
            " WHERE date_trunc('day', now()) = date_trunc('day', s.startTime) " +
            " AND s.userAccount.id = :userId"
    )
    fun getTodayHistories(userId: Long): List<StudyHistory>
}
