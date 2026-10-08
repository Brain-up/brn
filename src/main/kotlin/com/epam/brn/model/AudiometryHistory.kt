package com.epam.brn.model

import java.time.LocalDateTime
import java.util.LinkedList
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "audiometry_task_id", "startTime", "headphones"])],
)
class AudiometryHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var userAccount: UserAccount,
    @Column(nullable = false)
    var startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,
    // for speech audiometry
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audiometry_task_id")
    var audiometryTask: AudiometryTask,
    var executionSeconds: Int?,
    @Column(nullable = false)
    var tasksCount: Short,
    @Column(nullable = false)
    var rightAnswers: Int,
    var replaysCount: Int? = null,
    var repetitionIndex: Float? = null,
    var rightAnswersIndex: Float? = null,
    @OneToOne
    @JoinColumn(name = "headphones", nullable = false)
    var headphones: Headphones? = null,
    // for signal audiometry
    @OneToMany(mappedBy = "audiometryHistory", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val sinAudiometryResults: MutableList<SinAudiometryResult>? = LinkedList(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudiometryHistory
        if (id != other.id) return false
        if (startTime != other.startTime) return false
        return true
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
