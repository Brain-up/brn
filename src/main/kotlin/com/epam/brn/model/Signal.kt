package com.epam.brn.model

import com.epam.brn.dto.SignalTaskDto
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class Signal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String? = "",
    val url: String? = "",
    val frequency: Int? = null,
    val length: Int? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    var exercise: Exercise? = null,
) {
    fun toSignalDto() =
        SignalTaskDto(
            id = id,
            name = name,
            url = url,
            frequency = frequency,
            length = length,
        )
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
