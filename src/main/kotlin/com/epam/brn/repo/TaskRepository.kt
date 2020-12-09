package com.epam.brn.repo

import com.epam.brn.model.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TaskRepository : JpaRepository<Task, Long> {

    @Query("select DISTINCT t FROM Task t left JOIN FETCH t.answerOptions")
    fun findAllTasksWithJoinedAnswers(): List<Task>

    @Query("select DISTINCT t FROM Task t left JOIN FETCH t.answerOptions where t.exercise.id = ?1")
    fun findTasksByExerciseIdWithJoinedAnswers(id: Long): List<Task>

    @Query("select DISTINCT t " +
            "FROM Task t " +
            "left JOIN FETCH t.answerParts " +
            "left JOIN FETCH t.answerOptions " +
            "where t.id = ?1")
    override fun findById(id: Long): Optional<Task>
}
