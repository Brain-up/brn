package com.epam.brn.model

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["audiometryGroup", "frequencyZone"])])
data class AudiometryTask(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column
    var level: Int,
    @Column
    val audiometryGroup: String, // А, Б, В, Г
    @Column
    val frequencyZone: String,
    @Column
    val minFrequency: Int,
    @Column
    val maxFrequency: Int,

    var count: Int = 10,
    var showSize: Int = 9,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audiometry_id")
    var audiometry: Audiometry? = null,

    @ManyToMany(cascade = [CascadeType.MERGE, CascadeType.REFRESH])
    @JoinTable(
        name = "audiometry_task_resources",
        joinColumns = [JoinColumn(name = "audiometry_task_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "resource_id", referencedColumnName = "id")]
    )
    var answerOptions: MutableSet<Resource> = hashSetOf(),
) {
    override fun toString() =
        "AudiometryTask(id=$id, order=$level, group=$audiometryGroup, frequencyZone=$frequencyZone, minFrequency=$minFrequency, maxFrequency=$maxFrequency, count=$count, answerOptions=$answerOptions)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudiometryTask
        if (id != other.id) return false
        if (level != other.level) return false
        if (minFrequency != other.minFrequency) return false
        if (maxFrequency != other.maxFrequency) return false
        if (frequencyZone != other.frequencyZone) return false
        if (audiometryGroup != other.audiometryGroup) return false
        if (audiometry != other.audiometry) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + audiometryGroup.hashCode()
        result = 31 * result + frequencyZone.hashCode()
        return result
    }
}
