package com.epam.brn.repo

import com.epam.brn.model.Exercise
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ExerciseRepository : JpaRepository<Exercise, Long> {
    @Query(
        "SELECT DISTINCT e FROM Exercise e " +
            "JOIN FETCH e.subGroup sg " +
            "JOIN FETCH sg.series " +
            "WHERE sg.id = :subGroupId",
    )
    fun findExercisesWithSubGroupBySubGroupId(subGroupId: Long): List<Exercise>

    @Query(
        "SELECT DISTINCT e FROM Exercise e " +
            "JOIN FETCH e.subGroup sg " +
            "JOIN FETCH sg.series " +
            "WHERE e.id = :id",
    )
    fun findByIdWithSubGroup(id: Long): Exercise?

    fun findByNameAndLevel(
        name: String,
        level: Int,
    ): Exercise?

    fun findExerciseByNameAndLevel(
        name: String,
        level: Int,
    ): Optional<Exercise>

    @Query(
        "SELECT DISTINCT e FROM Exercise e " +
            "LEFT JOIN FETCH e.subGroup sg " +
            "LEFT JOIN FETCH sg.series " +
            "JOIN e.tasks t " +
            "JOIN t.answerOptions ao " +
            "WHERE UPPER(ao.word) like UPPER(concat('%',:word,'%'))",
    )
    fun findExercisesByWord(word: String): List<Exercise>

    @Query("SELECT e.id FROM Exercise e WHERE e.subGroup.id = :subGroupId")
    fun findExerciseIdsBySubGroupId(subGroupId: Long): List<Long>

    fun existsBySubGroupId(subGroupId: Long): Boolean

    override fun findById(id: Long): Optional<Exercise>

    @Query("SELECT e.subGroup.series.type FROM Exercise e WHERE e.id = :id")
    fun findTypeByExerciseId(id: Long): String?
}
