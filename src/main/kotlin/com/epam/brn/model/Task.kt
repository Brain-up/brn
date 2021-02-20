package com.epam.brn.model

import com.epam.brn.dto.TaskDtoFor1Series
import com.epam.brn.dto.TaskDtoFor2Series
import com.epam.brn.dto.TaskDtoFor3Series
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.OneToOne

@Entity
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String? = "",
    var serialNumber: Int? = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    var exercise: Exercise? = null,
    @OneToOne(cascade = [(CascadeType.MERGE)], optional = true)
    @JoinColumn(name = "resource_id")
    var correctAnswer: Resource? = null,
    @ManyToMany(cascade = [CascadeType.MERGE, CascadeType.REFRESH])
    @JoinTable(
        name = "task_resources",
        joinColumns = [JoinColumn(name = "task_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "resource_id", referencedColumnName = "id")]
    )
    var answerOptions: MutableSet<Resource> = hashSetOf(),
    @ManyToMany(cascade = [(CascadeType.MERGE)])
    @JoinTable(
        name = "answer_parts_resources",
        joinColumns = [JoinColumn(name = "task_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "resource_id", referencedColumnName = "id")]
    )
    var answerParts: MutableMap<Int, Resource> = mutableMapOf()
) {
    fun to1SeriesTaskDto() = TaskDtoFor1Series(
        id = id,
        exerciseType = ExerciseType.SINGLE_SIMPLE_WORDS,
        name = name,
        serialNumber = serialNumber,
        answerOptions = answerOptions.map { answer -> answer.toDto() }.toHashSet()
    )
    fun to2SeriesTaskDto(template: String? = "") = TaskDtoFor2Series(
        id = id,
        exerciseType = ExerciseType.WORDS_SEQUENCES,
        name = name,
        serialNumber = serialNumber,
        answerOptions = answerOptions.map { answer -> answer.toDto() }.groupBy { it.wordType },
        template = template
    )

    fun to3SeriesTaskDto(template: String? = "") = TaskDtoFor3Series(
        id = id,
        exerciseType = ExerciseType.SENTENCE,
        name = name,
        serialNumber = serialNumber,
        answerOptions = answerOptions.map { answer -> answer.toDto() }.groupBy { it.wordType },
        template = template,
        answerParts = answerParts.values.map { part -> part.toDto() },
        correctAnswer = correctAnswer!!.toDto()
    )
    fun to4SeriesTaskDto() = TaskDtoFor1Series(
        id = id,
        exerciseType = ExerciseType.PHRASES,
        name = name,
        serialNumber = serialNumber,
        answerOptions = answerOptions.map { answer -> answer.toDto() }.toHashSet()
    )

    override fun toString() = "Task(id=$id, name=$name, serialNumber=$serialNumber)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Task

        if (id != other.id) return false
        if (name != other.name) return false
        if (serialNumber != other.serialNumber) return false
        if (exercise != other.exercise) return false
        if (correctAnswer != other.correctAnswer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (serialNumber ?: 0)
        result = 31 * result + (exercise?.hashCode() ?: 0)
        result = 31 * result + (correctAnswer?.hashCode() ?: 0)
        return result
    }
}
