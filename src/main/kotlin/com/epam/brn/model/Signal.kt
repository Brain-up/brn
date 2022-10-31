package com.epam.brn.model

import com.epam.brn.dto.SignalTaskDto
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

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
    var exercise: Exercise? = null
) {
    fun toSignalDto() = SignalTaskDto(
        id = id,
        name = name,
        url = url,
        frequency = frequency,
        length = length
    )

    override fun toString() = "Signal(id=$id, name=$name, url=$url, frequency=$frequency, length=$length)"

}
