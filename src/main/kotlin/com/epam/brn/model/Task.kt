package com.epam.brn.model

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.dto.TaskDtoForSingleWords
import com.epam.brn.dto.TaskDtoForWordsSequences
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.SequenceGenerator
import javax.persistence.CascadeType
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.ManyToMany
import javax.persistence.JoinTable
import javax.persistence.FetchType

@Entity
data class Task(
    @Id
    @GeneratedValue(generator = "task_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
        name = "task_id_seq",
        sequenceName = "task_id_seq",
        allocationSize = 50
    )
    val id: Long? = null,
    val name: String? = "",
    var serialNumber: Int? = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    var exercise: Exercise? = null,
    @OneToOne(cascade = [(CascadeType.ALL)], optional = true)
    @JoinColumn(name = "resource_id")
    var correctAnswer: Resource? = null,
    @ManyToMany(cascade = [(CascadeType.ALL)])
    @JoinTable(
        name = "task_resources",
        joinColumns = [JoinColumn(name = "task_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "resource_id", referencedColumnName = "id")]
    )
    var answerOptions: MutableSet<Resource> = hashSetOf()
) {
    fun toSingleWordsDto() = TaskDtoForSingleWords(
        id = id,
        exerciseType = ExerciseTypeEnum.SINGLE_WORDS,
        name = name,
        serialNumber = serialNumber,
        correctAnswer = correctAnswer?.toDto(),
        answerOptions = answerOptions.map { answer -> answer.toDto() }.toMutableSet()
    )

    fun toSequenceWordsDto(template: String? = "") = TaskDtoForWordsSequences(
        id = id,
        exerciseType = ExerciseTypeEnum.WORDS_SEQUENCES,
        name = name,
        serialNumber = serialNumber,
        answerOptions = answerOptions.map { answer -> answer.toDto() }.groupBy { it.wordType },
        template = template
    )

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

    override fun toString() = "Task(id=$id, name=$name, serialNumber=$serialNumber)"
}