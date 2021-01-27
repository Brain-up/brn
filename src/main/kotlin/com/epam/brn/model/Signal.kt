package com.epam.brn.model

import com.epam.brn.dto.SignalDto
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator

@Entity
data class Signal(
    @Id
    @GeneratedValue(generator = "signal_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(
        name = "signal_id_seq",
        sequenceName = "signal_id_seq",
        allocationSize = 1
    )
    val id: Long? = null,
    val name: String? = "",
    val url: String? = "",
    val frequency: Int? = null,
    val length: Int? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    var exercise: Exercise? = null
) {
    fun toSignalDto() = SignalDto(
        id = id,
        name = name,
        url = url,
        frequency = frequency,
        length = length
    )

    override fun toString() = "Signal(id=$id, name=$name, url=$url, frequency=$frequency, length=$length)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Signal

        if (id != other.id) return false
        if (name != other.name) return false
        if (url != other.url) return false
        if (exercise != other.exercise) return false
        if (frequency != other.frequency) return false
        if (length != other.length) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (frequency ?: 0)
        result = 31 * result + (length ?: 0)
        return result
    }
}
