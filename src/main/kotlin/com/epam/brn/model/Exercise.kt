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
        allocationSize = 1
    )
    var id: Long? = null,
    var name: String,
    var template: String? = "",
    var level: Int = 0,
    var noiseLevel: Int = 0,
    var noiseUrl: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_group_id")
    var subGroup: SubGroup? = null,
) {
    @OneToMany(mappedBy = "exercise", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val tasks: MutableSet<Task> = LinkedHashSet()
    @OneToMany(mappedBy = "exercise", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val signals: MutableSet<Signal> = LinkedHashSet()

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
