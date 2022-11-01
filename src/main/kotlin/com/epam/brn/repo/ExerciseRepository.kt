package com.epam.brn.repo

import com.epam.brn.model.Exercise
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ExerciseRepository : JpaRepository<Exercise, Long> {

    fun findExercisesBySubGroupId(subGroupId: Long): List<Exercise>

    fun findByNameAndLevel(name: String, level: Int): Exercise?

    fun findExerciseByNameAndLevel(name: String, level: Int): Optional<Exercise>

    @Query(
        "SELECT e FROM Exercise e " +
            "JOIN e.tasks t " +
            "JOIN t.answerOptions ao " +
            "WHERE UPPER(ao.word) like UPPER(concat('%',:word,'%'))"
    )
    fun findExercisesByWord(word: String): List<Exercise>

    fun existsBySubGroupId(subGroupId: Long): Boolean

    override fun findById(id: Long): Optional<Exercise>

    @Query("SELECT e.subGroup.series.type FROM Exercise e WHERE e.id = :id")
    fun findTypeByExerciseId(id: Long): String
}
