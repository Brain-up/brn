package com.epam.brn.model

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.NoiseDto
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
    var template: String? = "",
    var level: Int? = 0,
    var noiseLevel: Int = 0,
    var noiseUrl: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_group_id")
    var subGroup: SubGroup? = null,
    @OneToMany(mappedBy = "exercise", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val tasks: MutableSet<Task> = LinkedHashSet(),
    @OneToMany(mappedBy = "exercise", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val signals: MutableSet<Signal> = LinkedHashSet()
) {
    fun toDto(available: Boolean = true) = ExerciseDto(
        seriesId = subGroup?.id,
        id = id,
        name = name,
        template = template,
        level = level,
        noise = NoiseDto(noiseLevel, noiseUrl),
        available = available,
        tasks = tasks.map { task -> ShortTaskDto(task.id) }.toMutableList(),
        signals = signals.map { signal -> signal.toSignalDto() }.toMutableList()
    )

    override fun toString() =
        "Exercise(id=$id, name='$name', level=$level, noiseLevel=$noiseLevel, " +
            "noiseUrl=$noiseUrl, template=$template)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Exercise

        if (id != other.id) return false
        if (name != other.name) return false
        if (template != other.template) return false
        if (level != other.level) return false
        if (noiseLevel != other.noiseLevel) return false
        if (noiseUrl != other.noiseUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + level.hashCode()
        result = 31 * result + (template?.hashCode() ?: 0)
        result = 31 * result + (noiseLevel)
        return result
    }

    fun addTask(task: Task) {
        tasks.add(task)
    }

    fun addTasks(tasks: List<Task>) {
        this.tasks.addAll(tasks)
    }

    fun addSignals(signals: List<Signal>) {
        this.signals.addAll(signals)
    }
}
