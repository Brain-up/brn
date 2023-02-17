package com.epam.brn.model

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.NoiseDto
import com.epam.brn.dto.response.ExerciseWithWordsResponse
import com.epam.brn.enums.ExerciseType
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["name", "level"])])
@EntityListeners(AuditingEntityListener::class)
class Exercise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String = "",
    var template: String? = "",
    var level: Int = 0,
    var noiseLevel: Int = 0,
    var noiseUrl: String = "",
    var active: Boolean = true,
    var playWordsCount: Int = 1,
    var wordsColumns: Int = 3,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_group_id")
    var subGroup: SubGroup? = null,
    @OneToMany(mappedBy = "exercise", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val tasks: MutableSet<Task> = LinkedHashSet(),
    @OneToMany(mappedBy = "exercise", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val signals: MutableSet<Signal> = LinkedHashSet()
) {
    @LastModifiedBy
    @Column(name = "changed_by")
    var changedBy: String = ""

    @Column(name = "changed_when")
    @LastModifiedDate
    var changedWhen: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
    fun toDto(available: Boolean = true) = ExerciseDto(
        seriesId = subGroup?.id,
        id = id,
        name = name,
        template = template,
        level = level,
        noise = NoiseDto(noiseLevel, noiseUrl),
        available = available,
        tasks = tasks.map { task -> task.toTaskResponse(ExerciseType.valueOf(this.subGroup!!.series.type)) },
        signals = signals.map { signal -> signal.toSignalDto() }.toMutableList(),
        active = active,
        changedBy = changedBy,
        changedWhen = changedWhen,
        playWordsCount = playWordsCount,
        wordsColumns = wordsColumns,
    )

    fun toDtoWithWords() = ExerciseWithWordsResponse(
        id = id,
        name = name,
        active = active,
        changedBy = changedBy,
        changedWhen = changedWhen,
        playWordsCount = playWordsCount,
        wordsColumns = wordsColumns,
        words = tasks.flatMap { it.answerOptions }.associate { it.id!! to it.word },
        subGroupName = subGroup?.name,
        seriesName = subGroup?.series?.name
    )

    override fun toString() =
        "Exercise(id=$id, name='$name', level=$level, noiseLevel=$noiseLevel, noiseUrl=$noiseUrl, " +
            "template=$template, active=$active, playWordsCount=$playWordsCount, wordsColumns=$wordsColumns)"

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
