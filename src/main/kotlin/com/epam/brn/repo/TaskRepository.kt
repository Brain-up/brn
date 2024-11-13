package com.epam.brn.repo

import com.epam.brn.model.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TaskRepository : JpaRepository<Task, Long> {

    @Query("""
        select DISTINCT t FROM Task t 
        left JOIN FETCH t.answerOptions ao 
        WHERE t IN (select t2 from Task t2)
    """)
    fun findAllTasksWithJoinedAnswers(): List<Task>

    @Query("""
        select DISTINCT t FROM Task t 
        left JOIN FETCH t.answerOptions ao 
        where t.exercise.id = :id
        AND t IN (select t2 from Task t2 where t2.exercise.id = :id)
    """)
    fun findTasksByExerciseIdWithJoinedAnswers(id: Long): List<Task>

    @Query("""
        select DISTINCT t FROM Task t 
        left JOIN FETCH t.answerParts ap
        left JOIN FETCH t.answerOptions ao
        where t.id = :id 
        AND t IN (select t2 from Task t2 where t2.id = :id)
    """)
    override fun findById(id: Long): Optional<Task>
}
