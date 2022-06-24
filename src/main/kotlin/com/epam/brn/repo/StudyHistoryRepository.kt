package com.epam.brn.repo

import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserDailyDetailStatisticsProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.sql.Date
import java.time.LocalDateTime

@Repository
interface StudyHistoryRepository : CrudRepository<StudyHistory, Long> {
//    @Query("SELECT DISTINCT s.exercise.id FROM StudyHistory s " +
//                " WHERE s.exercise.series.id = :seriesId and s.userAccount.id = :userId"
//    )
//    fun getDoneExercisesIdList(@Param("seriesId") seriesId: Long, @Param("userId") userId: Long): List<Long>

    @Query(
        "SELECT DISTINCT s.exercise FROM StudyHistory s " +
            " WHERE s.exercise.subGroup.id = :subGroupId and s.userAccount.id = :userId"
    )
    fun getDoneExercises(@Param("subGroupId") subGroupId: Long, @Param("userId") userId: Long): List<Exercise>

    @Query(
        "SELECT DISTINCT s.exercise FROM StudyHistory s " +
            " WHERE s.exercise.name = :name and s.userAccount.id = :userId"
    )
    fun getDoneExercisesByName(@Param("name") name: String, @Param("userId") userId: Long): List<Exercise>

    @Query(
        "SELECT DISTINCT s.exercise.id FROM StudyHistory s " +
            " WHERE s.userAccount.id = :userId"
    )
    fun getDoneExercisesIdList(@Param("userId") userId: Long): List<Long>

//    fun findByUserAccountIdAndExerciseId(userId: Long, exerciseId: Long): List<StudyHistory>
//    fun findLast1ByOrderByStartTime(): StudyHistory?
//    fun findLast1ByUserAccountIdAndExerciseIdOrderByStartTime(userId: Long, exerciseId: Long): StudyHistory?
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
            " WHERE date_trunc('day', s.startTime) >= :from " +
            " AND date_trunc('day', s.startTime) < :to " +
            " AND s.userAccount.id = :userId"
    )
    @Deprecated(message = "This is a legacy method. Use findAllByUserAccountIdAndStartTimeBetweenOrderByStartTime instead")
    fun getHistories(userId: Long, from: Date, to: Date): List<StudyHistory>

    @Query(
        value =
            """select
                s.name as series_name,
                count(1) as done_exercises,
                (select count(0) from (
                        select sh.exercise_id
                        from study_history sh, exercise e, sub_group sg, series ser
                        where sh.user_id = :userId and sh.start_time >= :from and sh.start_time <= :to
                        and sh.exercise_id = e.id and e.sub_group_id = sg.id and sg.exercise_series_id = ser.id and ser.id = s.id
                        group by sh.exercise_id
                        having sum(1) = 1) i) as done_exercises_successfully_from_first_time,
                sum(replays_count) as attempts,
                sum(t.tasks_count) as listen_words_count
                from (
                    select sh.exercise_id, sh.tasks_count, sum(sh.replays_count) replays_count
                    from study_history sh
                    where sh.user_id = :userId and sh.start_time >= :from and sh.start_time <= :to
                    group by sh.exercise_id, sh.tasks_count) t, exercise e, sub_group sg, series s
                where t.exercise_id = e.id and e.sub_group_id = sg.id and sg.exercise_series_id = s.id
                group by s.id, s.name """,
        nativeQuery = true
    )
    fun getDailyStatistics(userId: Long, from: LocalDateTime, to: LocalDateTime): List<UserDailyDetailStatisticsProjection>

    fun findAllByUserAccountIdAndStartTimeBetweenOrderByStartTime(
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
        "SELECT s FROM StudyHistory s " +
            " WHERE EXTRACT(YEAR FROM s.startTime) = :year " +
            " AND s.userAccount.id = :userId"
    )
    fun getYearStatistic(userId: Long, year: Int): List<StudyHistory>

    @Query(
        "SELECT s FROM StudyHistory s " +
            "WHERE EXTRACT(YEAR FROM s.startTime) = :year " +
            "AND EXTRACT(MONTH FROM s.startTime) = :month " +
            "AND EXTRACT(DAY FROM s.startTime) = :day " +
            "AND s.userAccount.id = :userId"
    )
    fun getDayStatistic(userId: Long, year: Int, month: Int, day: Int): List<StudyHistory>

    @Query(
        "select count (s) > 0 from StudyHistory s where s.userAccount.id = :userId"
    )
    fun isUserHasStatistics(userId: Long): Boolean
}
