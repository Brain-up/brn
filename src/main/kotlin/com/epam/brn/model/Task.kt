package com.epam.brn.model

import com.epam.brn.dto.response.TaskResponse
import com.epam.brn.dto.response.TaskWordsGroupResponse
import com.epam.brn.enums.ExerciseType
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
class Task(
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
    var answerOptions: MutableList<Resource> = mutableListOf(),
    @ManyToMany(cascade = [(CascadeType.MERGE)])
    @JoinTable(
        name = "answer_parts_resources",
        joinColumns = [JoinColumn(name = "task_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "resource_id", referencedColumnName = "id")]
    )
    var answerParts: MutableMap<Int, Resource> = mutableMapOf()
) {
    fun toTaskResponse(exerciseType: ExerciseType) = TaskResponse(
        id = id!!,
        exerciseType = exerciseType,
        name = name,
        serialNumber = serialNumber,
        answerOptions = answerOptions.map { answer -> answer.toResponse() }.toMutableList()
    )

    fun toWordsGroupSeriesTaskDto(template: String? = "") = TaskWordsGroupResponse(
        id = id!!,
        exerciseType = ExerciseType.WORDS_SEQUENCES,
        name = name,
        serialNumber = serialNumber,
        answerOptions = answerOptions.map { answer -> answer.toResponse() }.groupBy { it.wordType },
        template = template
    )

    fun toSentenceSeriesTaskDto(template: String? = "") = TaskWordsGroupResponse(
        id = id!!,
        exerciseType = ExerciseType.SENTENCE,
        name = name,
        serialNumber = serialNumber,
        answerOptions = answerOptions.map { answer -> answer.toResponse() }.groupBy { it.wordType },
        template = template,
    )

    override fun toString() = "Task(id=$id, name=$name, serialNumber=$serialNumber)"
}
