package com.epam.brn.model

import com.epam.brn.dto.response.AudiometryLopotkoTaskResponse
import com.epam.brn.dto.response.AudiometryMatrixTaskResponse
import com.epam.brn.dto.response.AudiometrySignalsTaskResponse
import com.epam.brn.enums.AudiometryType
import com.epam.brn.enums.EAR
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
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["audiometryGroup", "frequencyZone"])])
class AudiometryTask(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // == for Lopotko diagnostic
    var level: Int? = 0,
    val audiometryGroup: String? = null, // А, Б, В, Г
    val frequencyZone: String? = null,
    val minFrequency: Int? = null,
    val maxFrequency: Int? = null,

    var count: Int? = 10,
    var showSize: Int? = null,

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

    // == for frequency diagnostic
    val frequencies: String? = null,
    var ear: String = EAR.BOTH.name,
) {
    override fun toString() =
        "AudiometryTask(id=$id, order=$level, group=$audiometryGroup, frequencyZone=$frequencyZone, minFrequency=$minFrequency, maxFrequency=$maxFrequency, count=$count, ear =$ear)"

    fun toDto(): Any {
        return when (audiometry!!.audiometryType) {
            AudiometryType.SIGNALS.name -> AudiometrySignalsTaskResponse(
                id,
                EAR.valueOf(ear),
                frequencies!!.removeSurrounding("[", "]").split(", ").map { it.toInt() }
            )
            AudiometryType.SPEECH.name -> AudiometryLopotkoTaskResponse(
                id,
                level!!,
                audiometryGroup!!,
                frequencyZone!!,
                minFrequency!!,
                maxFrequency!!,
                count!!,
                showSize!!,
                answerOptions
            )
            AudiometryType.SPEECH.name -> AudiometryMatrixTaskResponse(
                id,
                count!!,
                answerOptions
            )
            else -> throw IllegalArgumentException("${audiometry!!.audiometryType} does not supported!")
        }
    }
}
