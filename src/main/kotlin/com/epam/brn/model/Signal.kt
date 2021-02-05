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
}
