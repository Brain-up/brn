package com.epam.brn.model

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.ShortTaskDto
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["name", "level"])])
data class Exercise(
    @Id
    @GeneratedValue(generator = "exercise_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
        name = "exercise_id_seq",
        sequenceName = "exercise_id_seq",
        allocationSize = 50
    )
    var id: Long? = null,
    var name: String = "",
    var description: String? = "",
    var template: String? = "",
    var exerciseType: String = "",
    var level: Short? = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_series_id")
    var series: Series? = null,
    @OneToMany(mappedBy = "exercise", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val tasks: MutableSet<Task> = LinkedHashSet()
) {
    fun toDto(available: Boolean? = null) = ExerciseDto(
        seriesId = series?.id,
        id = id,
        name = name,
        description = description,
        template = template,
        exerciseType = ExerciseTypeEnum.valueOf(exerciseType),
        level = level,
        available = available,
        tasks = tasks.map { task -> ShortTaskDto(task.id, "task/$exerciseType") }.toMutableSet()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Exercise
        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (level != other.level) return false
        if (template != other.template) return false
        if (exerciseType != other.exerciseType) return false
        if (series != other.series) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (level ?: 0)
        result = 31 * result + (template?.hashCode() ?: 0)
        result = 31 * result + (exerciseType.hashCode())
        result = 31 * result + series.hashCode()
        return result
    }

    override fun toString() =
        "Exercise(id=$id, name='$name', description=$description, level=$level, template=$template, exerciseType=$exerciseType)"
}
