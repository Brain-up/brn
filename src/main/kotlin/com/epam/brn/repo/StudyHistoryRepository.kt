package com.epam.brn.repo

import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.projection.FirstLastStudyView
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface StudyHistoryRepository : CrudRepository<StudyHistory, Long> {

    @Query(
        "SELECT DISTINCT s.exercise FROM StudyHistory s " +
            " WHERE s.exercise.subGroup.id = :subGroupId and s.userAccount.id = :userId"
    )
    fun getDoneExercises(@Param("subGroupId") subGroupId: Long, @Param("userId") userId: Long): List<Exercise>

    @Query(
        "SELECT DISTINCT s.exercise.id FROM StudyHistory s " +
            " WHERE s.userAccount.id = :userId"
    )
    fun getDoneExercisesIdList(@Param("userId") userId: Long): List<Long>

    @Query(
        "SELECT s FROM StudyHistory s " +
            " WHERE (s.userAccount.id, s.startTime) " +
            " IN (SELECT userAccount.id, max(startTime) " +
            "       FROM StudyHistory " +
            "       GROUP BY exercise.id, userAccount.id " +
            "       HAVING userAccount.id = :userId)"
    )
    fun findLastByUserAccountId(userId: Long): List<StudyHistory>

    @Query(
        "SELECT s FROM StudyHistory s " +
            " WHERE (s.userAccount.id, s.startTime) " +
            " IN (SELECT userAccount.id, max(startTime) " +
            "       FROM StudyHistory " +
            "       WHERE exercise.subGroup.id = :subGroupId  " +
            "       GROUP BY exercise.id, userAccount.id " +
            "       HAVING userAccount.id = :userId)"
    )
    fun findLastBySubGroupAndUserAccount(subGroupId: Long, userId: Long): List<StudyHistory>

    @Query(
        "SELECT s FROM StudyHistory s " +
            " WHERE (s.userAccount.id, s.exercise.id, s.startTime) " +
            " IN (SELECT userAccount.id, exercise.id, max(startTime) " +
            "       FROM StudyHistory " +
            "       GROUP BY exercise.id, userAccount.id " +
            "       HAVING userAccount.id = :userId and exercise.id in (:exerciseIds))"
    )
    fun findLastByUserAccountIdAndExercises(userId: Long, exerciseIds: List<Long>): List<StudyHistory>

    @Query(
        "SELECT s FROM StudyHistory s " +
            " WHERE (s.userAccount.id, s.exercise.id, s.startTime) " +
            " IN (SELECT userAccount.id, exercise.id, max(startTime) " +
            "       FROM StudyHistory " +
            "       GROUP BY exercise.id, userAccount.id " +
            "       HAVING userAccount.id = :userId and exercise.id = :exerciseId)"
    )
    fun findLastByUserAccountIdAndExerciseId(userId: Long, exerciseId: Long): StudyHistory?

    @Query(
        "SELECT MIN(s.startTime) as firstStudy, MAX(s.startTime) as lastStudy FROM StudyHistory s WHERE user_id=:userId"
    )
    fun findFirstAndLastVisitTimeByUserAccount(userId: Long?): FirstLastStudyView

    @Query(
        "SELECT COALESCE(sum(s.executionSeconds), 0) FROM StudyHistory s " +
            " WHERE date_trunc('day', s.startTime) = :day" +
            " AND s.userAccount.id = :userId"
    )
    fun getDayTimer(userId: Long, day: java.util.Date): Int

    @Query(
        "SELECT COALESCE(sum(COALESCE(s.executionSeconds, 0)), 0) FROM StudyHistory s " +
            " WHERE date_trunc('day', now()) = date_trunc('day', s.startTime)" +
            " AND s.userAccount.id = :userId"
    )
    fun getTodayDayTimer(userId: Long): Int

    @Query(
        "SELECT s FROM StudyHistory s " +
            " WHERE s.startTime >= :from " +
            " AND s.startTime <= :to " +
            " AND s.userAccount.id = :userId " +
            "ORDER BY s.startTime"
    )
    fun getHistories(
        userId: Long,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<StudyHistory>

    @Query(
        "SELECT s FROM StudyHistory s " +
            " WHERE EXTRACT(MONTH FROM s.startTime) = :month " +
            " AND EXTRACT(YEAR FROM s.startTime) = :year " +
            " AND s.userAccount.id = :userId"
    )
    fun getMonthHistories(userId: Long, month: Int, year: Int): List<StudyHistory>

    @Query(
        "SELECT s FROM StudyHistory s " +
            " WHERE date_trunc('day', now()) = date_trunc('day', s.startTime) " +
            " AND s.userAccount.id = :userId"
    )
    fun getTodayHistories(userId: Long): List<StudyHistory>

    @Query(
        "select count (s) > 0 from StudyHistory s where s.userAccount.id = :userId"
    )
    fun isUserHasStatistics(userId: Long): Boolean
}
